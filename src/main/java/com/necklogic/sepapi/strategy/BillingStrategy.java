package com.necklogic.sepapi.strategy;

import com.necklogic.sepapi.model.Student;

public interface BillingStrategy {
    void process(Student student);
}