package kr.co.module.api.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "reservation")
public class AdminReservationUpdateDto {
    @Id
    private Long reservationId;
    @NotNull
    private String reservationStatus;
    @NotNull
    private String adminId;
}
