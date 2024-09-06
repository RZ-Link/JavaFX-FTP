package org.example.demo.view.main;

import de.saxsys.mvvmfx.ViewModel;
import lombok.Data;
import org.example.demo.view.window.WindowView;

@Data
public class MainViewModel implements ViewModel {
    private WindowView windowView;
}
