package pl.speedapp.cargame.api.model;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ListResponseDto<T> extends BaseResponseDto {

    private List<T> data;

    @Builder
    public ListResponseDto(ErrorDto error, List<T> data) {
        super(error);
        this.data = data;
    }
}
