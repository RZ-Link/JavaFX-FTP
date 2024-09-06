package org.example.demo.view.login;

import de.saxsys.mvvmfx.ViewModel;
import lombok.Data;
import org.example.demo.view.window.WindowView;

@Data
public class LoginViewModel implements ViewModel {
    private WindowView windowView;
}
