package com.es.phoneshop.utils.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OrderValidator {
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String DELIVERY_DATE = "deliveryDate";
    public static final String PHONE = "phone";
    public static final String PAYMENT_METHOD = "paymentMethod";
    public static final String DELIVERY_ADDRESS = "deliveryAddress";
    private static final String NAME_REGEX = "^[A-Za-zА-Яа-я]+$";
    private static final String PHONE_REGEX = "^\\+375\\d{9}$";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String ERROR_INVALID_VALUE_MESSAGE = "Invalid value. Please write a valid value";

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String firstName;
        private String lastName;
        private String deliveryDate;
        private String phone;
        private String paymentMethod;
        private String deliveryAddress;
        private final Map<String, String> errors = new HashMap<>();

        private Builder() {

        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder deliveryDate(String deliveryDate) {
            this.deliveryDate = deliveryDate;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder deliveryAddress(String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }

        public Builder validateFirstName() {
            if (firstName == null || !firstName.matches(NAME_REGEX)) {
                errors.put(FIRST_NAME, ERROR_INVALID_VALUE_MESSAGE);
            }
            return this;
        }

        public Builder validateLastName() {
            if (lastName == null || !lastName.matches(NAME_REGEX)) {
                errors.put(LAST_NAME, ERROR_INVALID_VALUE_MESSAGE);
            }
            return this;
        }

        public Builder validateDeliveryDate() {
            try {
                if (deliveryDate == null) {
                    errors.put(DELIVERY_DATE, ERROR_INVALID_VALUE_MESSAGE);
                    return this;
                }

                LocalDate localDate = LocalDate.parse(deliveryDate, DateTimeFormatter.ofPattern(DATE_FORMAT));
                if (localDate.isBefore(LocalDate.now())) {
                    errors.put(DELIVERY_DATE, ERROR_INVALID_VALUE_MESSAGE);
                }
            } catch (DateTimeParseException e) {
                errors.put(DELIVERY_DATE, ERROR_INVALID_VALUE_MESSAGE);
            }
            return this;
        }

        public Builder validatePhone() {
            if (phone == null || !phone.matches(PHONE_REGEX)) {
                errors.put(PHONE, ERROR_INVALID_VALUE_MESSAGE);
            }
            return this;
        }

        public Builder validatePaymentMethod() {
            if (paymentMethod == null || paymentMethod.isEmpty()) {
                errors.put(PAYMENT_METHOD, ERROR_INVALID_VALUE_MESSAGE);
            }
            return this;
        }

        public Builder validateDeliveryAddress() {
            if (deliveryAddress == null || deliveryAddress.isEmpty()) {
                errors.put(DELIVERY_ADDRESS, ERROR_INVALID_VALUE_MESSAGE);
            }
            return this;
        }

        public Builder validateAll() {
            return validateFirstName()
                    .validateLastName()
                    .validateDeliveryDate()
                    .validatePhone()
                    .validatePaymentMethod()
                    .validateDeliveryAddress();
        }

        public Map<String, String> getErrors() {
            return Collections.unmodifiableMap(errors);
        }
    }
}
