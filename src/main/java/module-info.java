module haulyea {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens model;
    opens model.service;
    opens model.enums;
    opens view;
    opens view.panel;
    opens controller;
}