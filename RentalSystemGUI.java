import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import java.time.LocalDate;
import javafx.beans.property.SimpleStringProperty;

public class RentalSystemGUI extends Application {
    private RentalSystem rentalSystem = RentalSystem.getInstance();
    private ObservableList<Vehicle> vehicles = FXCollections.observableArrayList();
    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private ObservableList<RentalRecord> rentalHistory = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();


        Tab addVehicleTab = createAddVehicleTab();
        Tab addCustomerTab = createAddCustomerTab();
        Tab rentReturnTab = createRentReturnTab();
        Tab viewVehiclesTab = createViewVehiclesTab();
        Tab viewHistoryTab = createViewHistoryTab();

        tabPane.getTabs().addAll(addVehicleTab, addCustomerTab, rentReturnTab, viewVehiclesTab, viewHistoryTab);

        Scene scene = new Scene(tabPane, 800, 600);
        primaryStage.setTitle("Vehicle Rental System");
        primaryStage.setScene(scene);
        primaryStage.show();

        refreshData();
    }

    private Tab createAddVehicleTab() {
        Tab tab = new Tab("Add Vehicle");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("Car", "Motorcycle", "Truck", "SportCar"));
        typeCombo.setPromptText("Select Vehicle Type");

        TextField makeField = new TextField();
        makeField.setPromptText("Make");
        TextField modelField = new TextField();
        modelField.setPromptText("Model");
        TextField yearField = new TextField();
        yearField.setPromptText("Year");
        TextField licensePlateField = new TextField();
        licensePlateField.setPromptText("License Plate (optional)");

        VBox dynamicFields = new VBox(5);
        dynamicFields.setPadding(new Insets(5, 0, 5, 0));

        typeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            dynamicFields.getChildren().clear();
            if ("Car".equals(newVal)) {
                TextField seatsField = new TextField();
                seatsField.setPromptText("Number of Seats");
                dynamicFields.getChildren().add(seatsField);
            } else if ("SportCar".equals(newVal)) {
                TextField seatsField = new TextField();
                seatsField.setPromptText("Number of Seats");
                TextField hpField = new TextField();
                hpField.setPromptText("Horsepower");
                CheckBox turboCheck = new CheckBox("Has Turbo");
                dynamicFields.getChildren().addAll(seatsField, hpField, turboCheck);
            } else if ("Motorcycle".equals(newVal)) {
                CheckBox sidecarCheck = new CheckBox("Has Sidecar");
                dynamicFields.getChildren().add(sidecarCheck);
            } else if ("Truck".equals(newVal)) {
                TextField cargoField = new TextField();
                cargoField.setPromptText("Cargo Capacity");
                dynamicFields.getChildren().add(cargoField);
            }
        });

        Button addBtn = new Button("Add Vehicle");
        addBtn.setOnAction(e -> {
            try {
                String type = typeCombo.getValue();
                String make = makeField.getText();
                String model = modelField.getText();
                int year = Integer.parseInt(yearField.getText());
                String licensePlate = licensePlateField.getText().isEmpty() ? null : licensePlateField.getText();

                Vehicle vehicle = null;
                switch (type) {
                    case "Car":
                        TextField seatsField = (TextField) dynamicFields.getChildren().get(0);
                        int seats = Integer.parseInt(seatsField.getText());
                        vehicle = new Car(make, model, year, seats);
                        break;
                    case "SportCar":
                        TextField sportSeats = (TextField) dynamicFields.getChildren().get(0);
                        TextField hp = (TextField) dynamicFields.getChildren().get(1);
                        CheckBox turbo = (CheckBox) dynamicFields.getChildren().get(2);
                        vehicle = new SportCar(make, model, year, Integer.parseInt(sportSeats.getText()),
                                Integer.parseInt(hp.getText()), turbo.isSelected());
                        break;
                    case "Motorcycle":
                        CheckBox sidecar = (CheckBox) dynamicFields.getChildren().get(0);
                        vehicle = new Motorcycle(make, model, year, sidecar.isSelected());
                        break;
                    case "Truck":
                        TextField cargo = (TextField) dynamicFields.getChildren().get(0);
                        vehicle = new Truck(make, model, year, Double.parseDouble(cargo.getText()));
                        break;
                }
                if (vehicle != null) {
                    vehicle.setLicensePlate(licensePlate);
                    if (rentalSystem.addVehicle(vehicle)) {
                        showAlert("Success", "Vehicle added successfully.");
                        refreshData();
                    }
                }
            } catch (Exception ex) {
                showAlert("Error", "Invalid input: " + ex.getMessage());
            }
        });

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Make:"), 0, 1);
        grid.add(makeField, 1, 1);
        grid.add(new Label("Model:"), 0, 2);
        grid.add(modelField, 1, 2);
        grid.add(new Label("Year:"), 0, 3);
        grid.add(yearField, 1, 3);
        grid.add(new Label("License Plate:"), 0, 4);
        grid.add(licensePlateField, 1, 4);
        grid.add(new Label("Additional Fields:"), 0, 5);
        grid.add(dynamicFields, 1, 5);
        grid.add(addBtn, 1, 6);

        tab.setContent(grid);
        return tab;
    }

    private Tab createAddCustomerTab() {
        Tab tab = new Tab("Add Customer");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        TextField idField = new TextField();
        idField.setPromptText("Customer ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        Button addBtn = new Button("Add Customer");
        addBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                String name = nameField.getText();
                Customer customer = new Customer(id, name);
                if (rentalSystem.addCustomer(customer)) {
                    showAlert("Success", "Customer added successfully.");
                    refreshData();
                }
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid ID: must be a number.");
            }
        });

        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(addBtn, 1, 2);

        tab.setContent(grid);
        return tab;
    }

    private Tab createRentReturnTab() {
        Tab tab = new Tab("Rent/Return");
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        ComboBox<Customer> customerCombo = new ComboBox<>();
        customerCombo.setItems(customers);
        customerCombo.setPromptText("Select Customer");

        ComboBox<Vehicle> vehicleCombo = new ComboBox<>();
        vehicleCombo.setPromptText("Select Vehicle");
        vehicleCombo.setItems(vehicles.filtered(v -> v.getStatus() == Vehicle.VehicleStatus.AVAILABLE));

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        Button rentBtn = new Button("Rent");
        rentBtn.setOnAction(e -> {
            Customer customer = customerCombo.getValue();
            Vehicle vehicle = vehicleCombo.getValue();
            try {
                double amount = Double.parseDouble(amountField.getText());
                rentalSystem.rentVehicle(vehicle, customer, LocalDate.now(), amount);
                showAlert("Success", "Vehicle rented successfully.");
                refreshData();
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid amount.");
            }
        });

        Button returnBtn = new Button("Return");
        returnBtn.setOnAction(e -> {
            Customer customer = customerCombo.getValue();
            Vehicle vehicle = vehicleCombo.getValue();
            try {
                double fees = Double.parseDouble(amountField.getText());
                rentalSystem.returnVehicle(vehicle, customer, LocalDate.now(), fees);
                showAlert("Success", "Vehicle returned successfully.");
                refreshData();
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid fees.");
            }
        });

        HBox buttonBox = new HBox(10, rentBtn, returnBtn);
        vbox.getChildren().addAll(
                new Label("Customer:"), customerCombo,
                new Label("Vehicle:"), vehicleCombo,
                new Label("Amount/Fees:"), amountField,
                buttonBox
        );

        tab.setContent(vbox);
        return tab;
    }

    private Tab createViewVehiclesTab() {
        Tab tab = new Tab("View Vehicles");
        TableView<Vehicle> table = new TableView<>();
        table.setItems(vehicles);

        TableColumn<Vehicle, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> {
            Vehicle v = cell.getValue();
            String type = v instanceof Car ? "Car" : v instanceof Motorcycle ? "Motorcycle" : v instanceof Truck ? "Truck" : "SportCar";
            return javafx.beans.binding.Bindings.createObjectBinding(() -> type);
        });

        TableColumn<Vehicle, String> plateCol = new TableColumn<>("License Plate");
        plateCol.setCellValueFactory(new PropertyValueFactory<>("licensePlate"));

        TableColumn<Vehicle, String> makeCol = new TableColumn<>("Make");
        makeCol.setCellValueFactory(new PropertyValueFactory<>("make"));

        TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<Vehicle, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<Vehicle, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(typeCol, plateCol, makeCol, modelCol, yearCol, statusCol);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox vbox = new VBox(table);
        vbox.setPadding(new Insets(10));
        tab.setContent(vbox);
        return tab;
    }

    private Tab createViewHistoryTab() {
        Tab tab = new Tab("View History");
        TableView<RentalRecord> table = new TableView<>();
        table.setItems(rentalHistory);

        TableColumn<RentalRecord, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("recordType"));

        TableColumn<RentalRecord, String> plateCol = new TableColumn<>("Plate");
        plateCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getVehicle().getLicensePlate())
        );

        TableColumn<RentalRecord, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getCustomer().getCustomerName())
        );

        TableColumn<RentalRecord, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("recordDate"));

        TableColumn<RentalRecord, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        table.getColumns().addAll(typeCol, plateCol, customerCol, dateCol, amountCol);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox vbox = new VBox(table);
        vbox.setPadding(new Insets(10));
        tab.setContent(vbox);
        return tab;
    }

    private void refreshData() {
        vehicles.setAll(rentalSystem.getVehicles());
        customers.setAll(rentalSystem.getCustomers());
        rentalHistory.setAll(rentalSystem.getRentalHistory().getRentalHistory());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}