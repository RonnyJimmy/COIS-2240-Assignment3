import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;



public class VehicleRentalTest {
    
    private Car testCar;
    private Motorcycle testMotorcycle;
    private Truck testTruck;
    private Customer testCustomer;
    private RentalSystem rentalSystem;
    
    @BeforeEach
    void setUp() {
   
        testCar = new Car("Toyota", "Corolla", 2020, 5);
        testMotorcycle = new Motorcycle("Honda", "CBR", 2021, false);
        testTruck = new Truck("Ford", "F150", 2019, 1500.0);
    
        testCar.setLicensePlate("ABC123");
        testMotorcycle.setLicensePlate("XYZ789");
        testTruck.setLicensePlate("DEF456");
  
        testCustomer = new Customer(1, "John Doe");
       
        rentalSystem = RentalSystem.getInstance();
    }
    
    @Test
    void testLicensePlateValidation() {
       
        assertTrue(testCar.isValidPlate("AAA100"), "AAA100 should be a valid plate");
        assertTrue(testCar.isValidPlate("ABC567"), "ABC567 should be a valid plate");
        assertTrue(testCar.isValidPlate("ZZZ999"), "ZZZ999 should be a valid plate");
        
        assertFalse(testCar.isValidPlate(""), "Empty string should be invalid");
        assertFalse(testCar.isValidPlate(null), "Null should be invalid");
        assertFalse(testCar.isValidPlate("AAA1000"), "AAA1000 should be invalid (too many digits)");
        assertFalse(testCar.isValidPlate("ZZZ99"), "ZZZ99 should be invalid (too few digits)");
        assertFalse(testCar.isValidPlate("AA100"), "AA100 should be invalid (too few letters)");
        assertFalse(testCar.isValidPlate("AAAA100"), "AAAA100 should be invalid (too many letters)");
        assertFalse(testCar.isValidPlate("123ABC"), "123ABC should be invalid (wrong format)");
        assertFalse(testCar.isValidPlate("ABC-123"), "ABC-123 should be invalid (contains hyphen)");
        
        Exception exception1 = assertThrows(IllegalArgumentException.class, 
            () -> testCar.setLicensePlate("AAA1000"), 
            "Setting invalid plate AAA1000 should throw IllegalArgumentException");
        assertTrue(exception1.getMessage().contains("Invalid license plate format: AAA1000"));
        
        Exception exception2 = assertThrows(IllegalArgumentException.class, 
            () -> testMotorcycle.setLicensePlate("ZZ99"), 
            "Setting invalid plate ZZ99 should throw IllegalArgumentException");
        assertTrue(exception2.getMessage().contains("Invalid license plate format: ZZ99"));
        
        Exception exception3 = assertThrows(IllegalArgumentException.class, 
            () -> testTruck.setLicensePlate(""), 
            "Setting empty plate should throw IllegalArgumentException");
        assertTrue(exception3.getMessage().contains("Invalid license plate format:"));
        
        assertDoesNotThrow(() -> testCar.setLicensePlate("ABC123"), 
            "Setting valid plate ABC123 should not throw exception");
        assertEquals("ABC123", testCar.getLicensePlate());
        
        assertDoesNotThrow(() -> testMotorcycle.setLicensePlate("XYZ789"), 
            "Setting valid plate XYZ789 should not throw exception");
        assertEquals("XYZ789", testMotorcycle.getLicensePlate());
    }
    
    @Test
    void testRentAndReturnVehicle() {
      
        Vehicle vehicle = testCar;
        Customer customer = testCustomer;
        
       
        assertEquals(Vehicle.VehicleStatus.AVAILABLE, vehicle.getStatus(), 
            "Vehicle should initially be available");
        
       boolean rentSuccess = rentalSystem.rentVehicle(vehicle, customer, LocalDate.now(), 100.0);
        assertTrue(rentSuccess, "First rental should be successful");
        assertEquals(Vehicle.VehicleStatus.RENTED, vehicle.getStatus(), 
            "Vehicle status should be RENTED after successful rental");
        
        boolean secondRentAttempt = rentalSystem.rentVehicle(vehicle, customer, LocalDate.now(), 100.0);
        assertFalse(secondRentAttempt, "Second rental of the same vehicle should fail");
       
        boolean returnSuccess = rentalSystem.returnVehicle(vehicle, customer, LocalDate.now(), 0.0);
        assertTrue(returnSuccess, "Returning a rented vehicle should be successful");
        assertEquals(Vehicle.VehicleStatus.AVAILABLE, vehicle.getStatus(), 
            "Vehicle status should be AVAILABLE after successful return");
       
        boolean secondReturnAttempt = rentalSystem.returnVehicle(vehicle, customer, LocalDate.now(), 0.0);
        assertFalse(secondReturnAttempt, "Returning an already returned vehicle should fail");
    } 
    
     @Test
    void testSingletonRentalSystem() throws Exception {
      
        Constructor <RentalSystem>  constructor = RentalSystem.class.getDeclaredConstructor();
        
        int modifiers = constructor.getModifiers();
        assertTrue(Modifier.isPrivate(modifiers), 
            "RentalSystem constructor should be private to enforce Singleton pattern");
        
        RentalSystem instance1 = RentalSystem.getInstance();
        assertNotNull(instance1, "getInstance() should return a non-null RentalSystem instance");
        
        RentalSystem instance2 = RentalSystem.getInstance();
        assertSame(instance1, instance2, 
            "Multiple calls to getInstance() should return the same RentalSystem instance");
    }
}
  


    