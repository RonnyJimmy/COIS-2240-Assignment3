import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VehicleRentalTest {
    
    private Car testCar;
    private Motorcycle testMotorcycle;
    private Truck testTruck;
    
    @BeforeEach
    void setUp() {
  
        testCar = new Car("Toyota", "Corolla", 2020, 5);
        testMotorcycle = new Motorcycle("Honda", "CBR", 2021, false);
        testTruck = new Truck("Ford", "F150", 2019, 1500.0);
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
}