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
    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public RentalHistory getRentalHistory() {
        return rentalHistory;
    }

    private RentalSystem() {
    	 loadData();
    }
    
    public static RentalSystem getInstance() {
        if (instance == null) {
            instance = new RentalSystem();
        }
        return instance;
    }

    private void loadData() {
        loadVehicles();
        loadCustomers();
        loadRentalRecords();
    }

    private void loadVehicles() {
        File file = new File("vehicles.txt");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 6) continue;

                String className = parts[0];
                String licensePlate = "null".equals(parts[1]) ? null : parts[1];
                String make = parts[2];
                String model = parts[3];
                int year = Integer.parseInt(parts[4]);
                Vehicle.VehicleStatus status = Vehicle.VehicleStatus.valueOf(parts[5]);

                Vehicle vehicle = null;
                switch (className) {
                    case "Car":
                        if (parts.length < 7) continue;
                        vehicle = new Car(make, model, year, Integer.parseInt(parts[6]));
                        break;
                    case "SportCar":
                        if (parts.length < 9) continue;
                        vehicle = new SportCar(make, model, year,
                                Integer.parseInt(parts[6]),
                                Integer.parseInt(parts[7]),
                                Boolean.parseBoolean(parts[8]));
                        break;
                    case "Motorcycle":
                        if (parts.length < 7) continue;
                        vehicle = new Motorcycle(make, model, year,
                                Boolean.parseBoolean(parts[6]));
                        break;
                    case "Truck":
                        if (parts.length < 7) continue;
                        vehicle = new Truck(make, model, year,
                                Double.parseDouble(parts[6]));
                        break;
                    default:
                        continue;
                }

                if (vehicle != null) {
                    vehicle.setLicensePlate(licensePlate);
                    vehicle.setStatus(status);
                    vehicles.add(vehicle);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void loadCustomers() {
        File file = new File("customers.txt");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                int id = Integer.parseInt(parts[0]);
                customers.add(new Customer(id, parts[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRentalRecords() {
        File file = new File("rentalrecords.txt");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                String recordType = parts[0];
                String licensePlate = parts[1];
                int customerId = Integer.parseInt(parts[2]);
                LocalDate date = LocalDate.parse(parts[3]);
                double amount = Double.parseDouble(parts[4]);

                Vehicle vehicle = findVehicleByPlate(licensePlate);
                Customer customer = findCustomerById(String.valueOf(customerId));

                if (vehicle != null && customer != null) {
                    rentalHistory.addRecord(new RentalRecord(vehicle, customer, date, amount, recordType));
                }
            }
        } catch (IOException | DateTimeParseException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
    public boolean addVehicle(Vehicle vehicle) {
        String plate = vehicle.getLicensePlate();
        if (plate == null) {
            for (Vehicle v : vehicles) {
                if (v.getLicensePlate() == null) {
                    System.out.println("Error: Duplicate null license plate.");
                    return false;
                }
            }
        } else {
            Vehicle existing = findVehicleByPlate(plate);
            if (existing != null) {
                System.out.println("Error: License plate " + plate + " already exists.");
                return false;
            }
        }
        vehicles.add(vehicle);
        saveVehicle(vehicle);
        return true;
    }

    public boolean addCustomer(Customer customer) {
        int id = customer.getCustomerId();
        Customer existing = findCustomerById(String.valueOf(id));
        if (existing != null) {
            System.out.println("Error: Customer ID " + id + " already exists.");
            return false;
        }
        customers.add(customer);
        saveCustomer(customer);
        return true;
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

    public boolean rentVehicle(Vehicle vehicle, Customer customer, LocalDate date, double amount) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
            vehicle.setStatus(Vehicle.VehicleStatus.RENTED);
            RentalRecord record = new RentalRecord(vehicle, customer, date, amount, "RENT");
            rentalHistory.addRecord(record);
            saveRecord(record);
            System.out.println("Vehicle rented to " + customer.getCustomerName());
            return true;
        } else {
            System.out.println("Vehicle is not available.");
            return false;
        }
    }

    public boolean returnVehicle(Vehicle vehicle, Customer customer, LocalDate date, double extraFees) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.RENTED) {
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            RentalRecord record = new RentalRecord(vehicle, customer, date, extraFees, "RETURN");
            rentalHistory.addRecord(record);
            saveRecord(record);
            System.out.println("Vehicle returned by " + customer.getCustomerName());
            return true;
        } else {
            System.out.println("Vehicle is not rented.");
            return false;
        }
    }
    
    public void displayVehicles(boolean onlyAvailable) {
    	System.out.println("|     Type         |\tPlate\t|\tMake\t|\tModel\t|\tYear\t|");
    	System.out.println("---------------------------------------------------------------------------------");
    	 
        for (Vehicle v : vehicles) {
            if (!onlyAvailable || v.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
                System.out.println("|     " + (v instanceof Car ? "Car          " : "Motorcycle   ") + "|\t" + v.getLicensePlate() + "\t|\t" + v.getMake() + "\t|\t" + v.getModel() + "\t|\t" + v.getYear() + "\t|\t");
            }
        }
        System.out.println();
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
    
    public Customer findCustomerById(String id) {
        for (Customer c : customers)
            if (c.getCustomerId() == Integer.parseInt(id))
                return c;
        return null;
    }
}