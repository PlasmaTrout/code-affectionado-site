package org.bhn.training.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.bhn.training.api.Greeter;

@Component(name = "Simple String Greeter Component", immediate = true)
@Service(value = org.bhn.training.api.Greeter.class)
public class SimpleStringGreeterImpl implements Greeter {
    @Override
    public String greet() {
        return "Hello World!";
    }
}
