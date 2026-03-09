package com.necklogic.sepapi.strategy;

import com.necklogic.sepapi.model.Student;
import org.springframework.stereotype.Component;

@Component
public class CreditPackageStrategy implements BillingStrategy {

    @Override
    public void process(Student student) {
        // Implementation for credit package billing
    }

}