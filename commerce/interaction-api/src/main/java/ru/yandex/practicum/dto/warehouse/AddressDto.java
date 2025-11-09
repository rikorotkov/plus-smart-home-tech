package ru.yandex.practicum.dto.warehouse;

import lombok.Builder;
import lombok.Data;

import java.security.SecureRandom;
import java.util.Random;

@Data
@Builder
public class AddressDto {

    private static final String[] ADDRESSES =
            new String[]{"ADDRESS_1", "ADDRESS_2"};

    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, 1)];

    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;

    public static String getCurrentAddress() {
        return CURRENT_ADDRESS;
    }
}