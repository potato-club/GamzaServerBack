package gamza.project.gamzaweb.service.impl;

import gamza.project.gamzaweb.dto.user.request.RequestUserLoginDto;
import gamza.project.gamzaweb.dto.user.request.RequestUserSignUpDto;
import gamza.project.gamzaweb.dto.user.response.ResponseNotApproveDto;
import gamza.project.gamzaweb.dto.user.response.ResponseUser;
import gamza.project.gamzaweb.dto.user.response.ResponseUserList;
import gamza.project.gamzaweb.Entity.Enums.UserRole;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.BadRequestException;
import gamza.project.gamzaweb.error.requestError.NotFoundException;
import gamza.project.gamzaweb.error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.repository.UserRepository;
import gamza.project.gamzaweb.service.Interface.UserService;
import gamza.project.gamzaweb.service.jwt.JwtTokenProvider;
import gamza.project.gamzaweb.service.jwt.RedisJwtService;
import gamza.project.gamzaweb.validate.UserValidate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisJwtService redisJwtService;
    private final UserValidate userValidate;

    @Override
    @Transactional
    public void signUp(RequestUserSignUpDto dto, HttpServletResponse response) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UnAuthorizedException("S404", ErrorCode.NOT_ALLOW_ACCESS_EXCEPTION);
        }

        if (userRepository.existsByStudentId(dto.getStudentId())) {
            throw new BadRequestException("There is a duplicate student number.", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        dto.setPassword(passwordEncoder.encode(dto.getPassword()));

        UserEntity user = dto.toEntity();
        userRepository.save(user);
    }

    @Override
    public void login(RequestUserLoginDto dto, HttpServletResponse response) {

        if (!userRepository.existsByEmail(dto.getEmail())) {
            throw new UnAuthorizedException("L401-1", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        UserEntity user = userRepository.findByEmail(dto.getEmail()).orElseThrow();

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnAuthorizedException("L401-2", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        setTokenInHeader(dto.getEmail(), response);
    }

    @Override
    public void logout(HttpServletRequest request) {
        redisJwtService.delValues(jwtTokenProvider.resolveRefreshToken(request));
    }

    @Override
    public void reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
        jwtTokenProvider.validateRefreshToken(refreshToken);

        String userEmail = jwtTokenProvider.extractUserEmail(refreshToken);

        if(redisJwtService.isTokenValid(refreshToken)) {
            throw new UnAuthorizedException("로그아웃된 토큰입니다. 다시 로그인해주세요.", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        String newAT = jwtTokenProvider.reissueAT(refreshToken, response);
        String newRT = jwtTokenProvider.reissueRT(refreshToken, response);

        jwtTokenProvider.setHeaderAccessToken(response, newAT);
        jwtTokenProvider.setHeaderRefreshToken(response, newRT);

        redisJwtService.setValues(newRT, userEmail);
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

        redisJwtService.setValues(refreshToken, email); // add
    }

    @Override
    public void approve(HttpServletRequest request, Long id) {
        userValidate.validateUserRole(request);
        UserEntity user = userRepository.findById(id).orElseThrow();
        user.approveUserStatus(); // USER -> MEMBER

        userRepository.save(user);

    }

    @Override
    public void notApprove(HttpServletRequest request, Long id) {
        userValidate.validateUserRole(request);

        UserEntity user = userRepository.findById(id).orElseThrow();
        user.notApproveUserStatus();

        userRepository.save(user);
    }

    @Override
    public ResponseUserList userList() {

        List<UserEntity> userEntities = userRepository.findAllByOrderByFamilyNameAsc();

        List<ResponseUser> responseUsers = userEntities.stream()
                .map(user -> ResponseUser.builder()
                        .id(user.getId())
                        .name(user.getFamilyName() + user.getGivenName())
                        .studentId(user.getStudentId())
                        .build())
                .toList();

        return ResponseUserList.builder()
                .userList(responseUsers)
                .build();

    }

    @Override
    public Page<ResponseNotApproveDto> approveList(HttpServletRequest request, Pageable pageable) {
        userValidate.validateUserRole(request);

        Page<UserEntity> userEntities = userRepository.findByUserRole(UserRole.USER, pageable);

        return userEntities.map(ResponseNotApproveDto::new);
    }


}
