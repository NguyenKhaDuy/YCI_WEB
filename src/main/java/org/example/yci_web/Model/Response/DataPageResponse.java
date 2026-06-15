package org.example.yci_web.Model.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataPageResponse <T>{
    private T data;
    private Integer totalPage;
    private Integer currentPage;
    private String message;
    private HttpStatus status;
}
