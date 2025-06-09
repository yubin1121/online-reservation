package kr.co.module.api.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "reservation")
public class AdminReservationUpdateDto {
    @Id
    private String reservationId;
    @NotNull
    private String reservationStatus;
    @NotNull
    private String adminId;
}
