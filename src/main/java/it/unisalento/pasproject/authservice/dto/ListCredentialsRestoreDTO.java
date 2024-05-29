package it.unisalento.pasproject.authservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListCredentialsRestoreDTO {
    private List<CredentialsRestoreDTO> list;
}
