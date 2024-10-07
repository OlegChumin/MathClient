package org.example.mathclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@Data
public class OperationRequest {
    private double a;
    private double b;
}