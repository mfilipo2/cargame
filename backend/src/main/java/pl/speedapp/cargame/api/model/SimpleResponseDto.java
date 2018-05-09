package pl.speedapp.cargame.api.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SimpleResponseDto<T> extends BaseResponseDto {

    private T data;

    @Builder
    public SimpleResponseDto(ErrorDto error, T data) {
        super(error);
        this.data = data;
    }
}
