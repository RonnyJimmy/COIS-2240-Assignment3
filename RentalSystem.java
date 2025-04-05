import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.io.*;
import java.time.format.DateTimeParseException;

public class RentalSystem {
	private static RentalSystem instance;
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private RentalHistory rentalHistory = new RentalHistory();

    private RentalSystem() {
    	loadData();
    }
    private void loadData() {
        loadVehicles();
        loadCustomers();
        loadRentalRecords();
    }
    private void loadVehicles() {
        try (BufferedReader reader = new BufferedReader(new FileReader("vehicles.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 5) continue;

                String plate = parts[0];
                String make = parts[1];
                String model = parts[2];
                int year = Integer.parseInt(parts[3]);
                String type = parts[4];

                Vehicle vehicle = createVehicle(type, make, model, year);
                if (vehicle != null) {
                    vehicle.setLicensePlate(plate);
                    vehicles.add(vehicle);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No existing vehicle data found.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading vehicles: " + e.getMessage());
        }
    }
    private Vehicle createVehicle(String type, String make, String model, int year) {
        switch (type) {
            case "Car": return new Car(make, model, year, 4); // Default seats
            case "Motorcycle": return new Motorcycle(make, model, year, false); // Default no sidecar
            case "Truck": return new Truck(make, model, year, 1000.0); // Default capacity
            case "SportCar": return new SportCar(make, model, year, 4, 300, false); // Default values
            default: return null;
        }
    }
    private void loadCustomers() {
        try (BufferedReader reader = new BufferedReader(new FileReader("customers.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 2) continue;

                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                customers.add(new Customer(id, name));
            }
        } catch (FileNotFoundException e) {
            System.out.println("No existing customer data found.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading customers: " + e.getMessage());
        }
    }
    private void loadRentalRecords() {
        try (BufferedReader reader = new BufferedReader(new FileReader("rentalrecords.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 5) continue;

                String plate = parts[0];
                String customerName = parts[1];
                LocalDate date = LocalDate.parse(parts[2]);
                double amount = Double.parseDouble(parts[3]);
                String type = parts[4];

                Vehicle vehicle = findVehicleByPlate(plate);
                Customer customer = findCustomerByName(customerName);
                if (vehicle != null && customer != null) {
                    rentalHistory.addRecord(new RentalRecord(vehicle, customer, date, amount, type));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No existing rental history found.");
        } catch (IOException | DateTimeParseException | NumberFormatException e) {
            System.err.println("Error loading rental history: " + e.getMessage());
        }
    }

    public static RentalSystem getInstance() {
        if (instance == null) {
            instance = new RentalSystem();
        }
        return instance;
    }
    
    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        saveVehicle(vehicle);
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
        saveCustomer(customer);
    }
    
    private void saveVehicle(Vehicle vehicle) {
        try (PrintWriter out = new PrintWriter(new FileWriter("vehicles.txt", true))) {
            String type = "Vehicle";
            if (vehicle instanceof Car) type = "Car";
            else if (vehicle instanceof Motorcycle) type = "Motorcycle";
            else if (vehicle instanceof Truck) type = "Truck";
            else if (vehicle instanceof SportCar) type = "SportCar";
            
            out.printf("%s,%s,%s,%d,%s%n", 
                vehicle.getLicensePlate(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                type);
        } catch (IOException e) {
            System.err.println("Error saving vehicle: " + e.getMessage());
        }
    }

    private void saveCustomer(Customer customer) {
        try (PrintWriter out = new PrintWriter(new FileWriter("customers.txt", true))) {
            out.printf("%d,%s%n",
                customer.getCustomerId(),
                customer.getCustomerName());
        } catch (IOException e) {
            System.err.println("Error saving customer: " + e.getMessage());
        }
    }

    private void saveRecord(RentalRecord record) {
        try (PrintWriter out = new PrintWriter(new FileWriter("rentalrecords.txt", true))) {
            out.printf("%s,%s,%s,%.2f,%s%n",
                record.getVehicle().getLicensePlate(),
                record.getCustomer().getCustomerName(),
                record.getRecordDate(),
                record.getTotalAmount(),
                record.getRecordType());
        } catch (IOException e) {
            System.err.println("Error saving record: " + e.getMessage());
        }
    }

    public void rentVehicle(Vehicle vehicle, Customer customer, LocalDate date, double amount) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
            vehicle.setStatus(Vehicle.VehicleStatus.RENTED);
            RentalRecord record = new RentalRecord(vehicle, customer, date, amount, "RENT");
            rentalHistory.addRecord(record);
            saveRecord(record);
            System.out.println("Vehicle rented to " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not available for renting.");
        }
    }

    public void returnVehicle(Vehicle vehicle, Customer customer, LocalDate date, double extraFees) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.RENTED) {
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            RentalRecord record = new RentalRecord(vehicle, customer, date, extraFees, "RETURN");
            rentalHistory.addRecord(record);
            saveRecord(record);
            System.out.println("Vehicle returned by " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not rented.");
        }
    }    

    public void displayAvailableVehicles() {
    	System.out.println("|     Type         |\tPlate\t|\tMake\t|\tModel\t|\tYear\t|");
    	System.out.println("---------------------------------------------------------------------------------");
    	 
        for (Vehicle v : vehicles) {
            if (v.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
                System.out.println("|     " + (v instanceof Car ? "Car          " : "Motorcycle   ") + "|\t" + v.getLicensePlate() + "\t|\t" + v.getMake() + "\t|\t" + v.getModel() + "\t|\t" + v.getYear() + "\t|\t");
            }
        }
        System.out.println();
    }
    
    public void displayAllVehicles() {
        for (Vehicle v : vehicles) {
            System.out.println("  " + v.getInfo());
        }
    }

    public void displayAllCustomers() {
        for (Customer c : customers) {
            System.out.println("  " + c.toString());
        }
    }
    
    public void displayRentalHistory() {
        for (RentalRecord record : rentalHistory.getRentalHistory()) {
            System.out.println(record.toString());
        }
    }
    
    public Vehicle findVehicleByPlate(String plate) {
        for (Vehicle v : vehicles) {
            if (v.getLicensePlate().equalsIgnoreCase(plate)) {
                return v;
            }
        }
        return null;
    }
    
    public Customer findCustomerById(int id) {
        for (Customer c : customers)
            if (c.getCustomerId() == id)
                return c;
        return null;
    }

    public Customer findCustomerByName(String name) {
        for (Customer c : customers)
            if (c.getCustomerName().equalsIgnoreCase(name))
                return c;
        return null;
    }
}