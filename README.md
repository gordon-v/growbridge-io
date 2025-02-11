# growbridge-io

## Command Line Interface

### Prerequisites

- **Java Development Kit (JDK 17)** installed
- **Apache Maven** installed
- **Git** installed (if cloning from GitHub)
- **IntelliJ IDEA** (Recommended for ease of setup)

### Setup Instructions

#### 1. Clone the Repository

```sh
git clone https://github.com/gordon-v/growbridge-io.git
cd growbridge-io
```

#### 2. Open the Project in IntelliJ IDEA
- Launch **IntelliJ IDEA** and open the pom.xml file.
- IntelliJ will automatically download all necessary dependencies.
- *If IntelliJ highlights SQL strings as *warnings**, you can ignore them for now. We will configure the database connection in the next steps.

#### 3. Configure Database Connection
- Navigate to config/db.config.
- Fill in the required database connection details (e.g., HOST, NAME, USER).
 -These details will be used at runtime to establish a connection to the PostgreSQL database.

#### 4. Build and Run the Application
- Open MarketingApp.java.
- Click the Run or Debug button to launch the application.
