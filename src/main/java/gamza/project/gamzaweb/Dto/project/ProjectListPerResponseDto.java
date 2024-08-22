package gamza.project.gamzaweb.Dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectListPerResponseDto {
    private int size;
    private List<ProjectPerResponseDto> contents;
}
