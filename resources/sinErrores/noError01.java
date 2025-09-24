/// [SinError]
class JavaSyntaxDemo {

    // Class-level variable
    private int count;

    // Constructor
    public JavaSyntaxDemo(int initialCount) {
        this.count = initialCount;
    }

    // Method that demonstrates basic conditional logic
    public void checkCount() {
        if (count > 0) {
            System.println("Count is positive.");
        } else if (count < 0) {
            System.println("Count is negative.");
        } else {
            System.println("Count is zero.");
        }
    }

    // Method to demonstrate a simple loop (for loop)
    public void countDown() {
        for (int i = count; i >= 0; --i) {
            System.println("Counting down: " + i);
        }
    }

    // Method to demonstrate array usage
    public void printArray() {
        String fruits = "Apple";
        for (String fruit : fruits) {
            System.println(fruit);
        }
    }

    // Method with a return type (demonstrating a simple mathematical operation)
    public int doubleCount() {
        return count * 2;
    }

    // Main method to run the program
    public static void main(String args) {
        JavaSyntaxDemo demo = new JavaSyntaxDemo(5);

        // Call method to check the count
        demo.checkCount();

        // Call method to count down
        demo.countDown();

        // Call method to print array of fruits
        demo.printArray();

        // Call method to double the count and print it
        System.println("Doubled count: " + demo.doubleCount());
    }
}