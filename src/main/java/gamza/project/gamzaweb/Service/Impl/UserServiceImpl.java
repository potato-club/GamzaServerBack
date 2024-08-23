package gamza.project.gamzaweb.Service.Impl;

import gamza.project.gamzaweb.Dto.User.RequestUserLoginDto;
import gamza.project.gamzaweb.Dto.User.RequestUserSignUpDto;
import gamza.project.gamzaweb.Dto.User.ResponseNotApproveDto;
import gamza.project.gamzaweb.Entity.Enums.UserRole;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.NotFoundException;
import gamza.project.gamzaweb.Error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.Repository.UserRepository;
import gamza.project.gamzaweb.Service.Interface.UserService;
import gamza.project.gamzaweb.Service.Jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public void signUp(RequestUserSignUpDto dto, HttpServletResponse response) {
        if(userRepository.existsByEmail(dto.getEmail())) {
            throw new UnAuthorizedException("S404", ErrorCode.NOT_ALLOW_ACCESS_EXCEPTION);
        }
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));

        UserEntity user = dto.toEntity();
        userRepository.save(user);
    }

    @Override
    public void login(RequestUserLoginDto dto, HttpServletResponse response) {

        if(!userRepository.existsByEmail(dto.getEmail())) {
            throw new UnAuthorizedException("L401-1", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        UserEntity user = userRepository.findByEmail(dto.getEmail()).orElseThrow();

        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnAuthorizedException("L401-2", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        setTokenInHeader(dto.getEmail(), response);
    }

    @Override
    public void reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
        jwtTokenProvider.validateRefreshToken(refreshToken);

        String newAT = jwtTokenProvider.reissueAT(refreshToken, response);
        jwtTokenProvider.setHeaderAccessToken(response, newAT);
    }

    @Override
    public void setTokenInHeader(String email, HttpServletResponse response) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("5003", ErrorCode.NOT_ALLOW_ACCESS_EXCEPTION));

        UserRole role = user.getUserRole();

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), role);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), role);

        jwtTokenProvider.setHeaderAccessToken(response, accessToken);
        jwtTokenProvider.setHeaderRefreshToken(response, refreshToken);
    }

    @Override
    public void approve(HttpServletRequest request, Long id) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        String role = jwtTokenProvider.extractRole(token);
//        System.out.println("role : " + role); // admin 0, member 1, user 2

        if(!role.equals("0")) {
            throw new UnAuthorizedException("401 NOT ADMIN", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        UserEntity user = userRepository.findById(id).orElseThrow();
        user.approveUserStatus(); // USER -> MEMBER

        userRepository.save(user);

    }

    @Override
    public Page<ResponseNotApproveDto> approveList(HttpServletRequest request, Pageable pageable) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        String userRole = jwtTokenProvider.extractRole(token);

        if(!userRole.equals("0")) {
            throw new UnAuthorizedException("401 NOT ADMIN", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        Page<UserEntity> userEntities = userRepository.findByUserRole(UserRole.USER, pageable);

        return userEntities.map(ResponseNotApproveDto::new);

    }


}
