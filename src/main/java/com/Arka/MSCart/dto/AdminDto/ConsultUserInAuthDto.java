package com.Arka.MSCart.dto.AdminDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultUserInAuthDto {
    private String name;
    private String email;

}
