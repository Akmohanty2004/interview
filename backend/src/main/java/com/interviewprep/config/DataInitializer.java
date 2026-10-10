package com.interviewprep.config;

import com.interviewprep.entity.*;
import com.interviewprep.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserBadgeRepository userBadgeRepository;
    private final MockTestRepository mockTestRepository;
    private final SubjectNoteRepository subjectNoteRepository;

    public DataInitializer(UserRepository userRepository,
                           SubjectRepository subjectRepository,
                           TopicRepository topicRepository,
                           QuestionRepository questionRepository,
                           PasswordEncoder passwordEncoder,
                           UserBadgeRepository userBadgeRepository,
                           MockTestRepository mockTestRepository,
                           SubjectNoteRepository subjectNoteRepository) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
        this.passwordEncoder = passwordEncoder;
        this.userBadgeRepository = userBadgeRepository;
        this.mockTestRepository = mockTestRepository;
        this.subjectNoteRepository = subjectNoteRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Initializing InterviewPrep users, subjects, and questions...");

        // 1. Seed or Update Primary Admin: pradhanaiswariya1@gmail.com / Aiswariya00
        userRepository.findByEmail("pradhanaiswariya1@gmail.com").ifPresentOrElse(
            user -> {
                user.setPassword(passwordEncoder.encode("Aiswariya00"));
                user.setRole(Role.ROLE_ADMIN);
                user.setVerified(true);
                userRepository.save(user);
                logger.info("Verified primary admin: pradhanaiswariya1@gmail.com");
            },
            () -> {
                User admin = new User("Aiswariya Pradhan", "pradhanaiswariya1@gmail.com", passwordEncoder.encode("Aiswariya00"), Role.ROLE_ADMIN);
                admin.setCollege("Global University");
                admin.setDegree("B.Tech");
                admin.setBranch("Computer Science & Engineering");
                admin.setGraduationYear(2025);
                admin.setVerified(true);
                userRepository.save(admin);
                logger.info("Created primary admin: pradhanaiswariya1@gmail.com");
            }
        );

        // 2. Seed or Update Secondary Admin: admin@interviewprep.com / Admin@123
        userRepository.findByEmail("admin@interviewprep.com").ifPresentOrElse(
            user -> {
                user.setPassword(passwordEncoder.encode("Admin@123"));
                user.setRole(Role.ROLE_ADMIN);
                user.setVerified(true);
                userRepository.save(user);
            },
            () -> {
                User defaultAdmin = new User("Platform Administrator", "admin@interviewprep.com", passwordEncoder.encode("Admin@123"), Role.ROLE_ADMIN);
                defaultAdmin.setCollege("Global University");
                defaultAdmin.setDegree("M.Tech");
                defaultAdmin.setBranch("Computer Science");
                defaultAdmin.setGraduationYear(2022);
                defaultAdmin.setVerified(true);
                userRepository.save(defaultAdmin);
            }
        );

        // 3. Seed Students Directory
        List<User> students = new ArrayList<>();
        students.add(seedOrUpdateStudent("Alex Morgan", "student@interviewprep.com", "Student@123", "Stanford Institute of Technology", "B.Tech", "Computer Science & Engineering", 2025, 4));
        students.add(seedOrUpdateStudent("Priya Sharma", "priya.sharma@techuniv.edu", "Student@123", "National Institute of Technology", "B.Tech", "Information Technology", 2025, 6));
        students.add(seedOrUpdateStudent("Rahul Verma", "rahul.verma@iitdelhi.ac.in", "Student@123", "IIT Delhi", "B.Tech", "Computer Science", 2024, 3));
        students.add(seedOrUpdateStudent("Ananya Patel", "ananya.patel@bits.ac.in", "Student@123", "BITS Pilani", "B.E.", "Software Engineering", 2025, 5));
        students.add(seedOrUpdateStudent("Rohan Gupta", "rohan.gupta@iiit.ac.in", "Student@123", "IIIT Hyderabad", "M.Tech", "Data Science", 2024, 2));

        // 4. Seed Subjects, Topics & Questions if not yet seeded
        Map<String, Subject> subjectMap = new HashMap<>();
        Map<String, Topic> topicMap = new HashMap<>();
        if (subjectRepository.count() == 0 || questionRepository.count() == 0) {
            logger.info("Seeding InterviewPrep database with subjects, topics, and high-yield questions...");
            createSubjectAndTopics(subjectMap, topicMap);
            seedQuestions(subjectMap, topicMap);
        } else {
            subjectRepository.findAll().forEach(s -> subjectMap.put(s.getName(), s));
            topicRepository.findAll().forEach(t -> topicMap.put(t.getName(), t));
        }

        // 5. Seed Sample Completed Interviews if not yet seeded
        if (mockTestRepository.count() == 0) {
            logger.info("Seeding sample completed student mock test interviews...");
            seedSampleInterviews(students, subjectMap, topicMap);
        }

        // 6. Seed Sample Subject Note Links if not yet seeded
        if (subjectNoteRepository.count() == 0) {
            logger.info("Seeding starter subject note links...");
            seedSubjectNotes(subjectMap);
        }

        logger.info("Database initialization completed. Total users: {}, subjects: {}, topics: {}, questions: {}, completed mock tests: {}, note links: {}.",
                userRepository.count(), subjectRepository.count(), topicRepository.count(), questionRepository.count(), mockTestRepository.count(), subjectNoteRepository.count());
    }

    private User seedOrUpdateStudent(String name, String email, String rawPassword, String college, String degree, String branch, int gradYear, int streak) {
        return userRepository.findByEmail(email).map(existing -> {
            existing.setName(name);
            existing.setCollege(college);
            existing.setDegree(degree);
            existing.setBranch(branch);
            existing.setGraduationYear(gradYear);
            existing.setStreakDays(streak);
            existing.setVerified(true);
            return userRepository.save(existing);
        }).orElseGet(() -> {
            User s = new User(name, email, passwordEncoder.encode(rawPassword), Role.ROLE_STUDENT);
            s.setCollege(college);
            s.setDegree(degree);
            s.setBranch(branch);
            s.setGraduationYear(gradYear);
            s.setStreakDays(streak);
            s.setVerified(true);
            s.setLastPracticeDate(LocalDateTime.now().minusHours(10));
            s = userRepository.save(s);

            UserBadge badge = new UserBadge(s, "WELCOME_PREPPER", "Early Adopter", "Joined the InterviewPrep platform and initiated career readiness", "🚀");
            userBadgeRepository.save(badge);
            return s;
        });
    }

    private void seedSampleInterviews(List<User> students, Map<String, Subject> subjectMap, Map<String, Topic> topicMap) {
        if (students.isEmpty() || subjectMap.isEmpty()) return;

        Subject java = subjectMap.get("Java");
        Subject spring = subjectMap.get("Spring Boot");
        Subject react = subjectMap.get("React");
        Subject mysql = subjectMap.get("MySQL & SQL");
        if (mysql == null) mysql = subjectMap.get("MySQL");
        Subject dsa = subjectMap.get("Data Structures & Algorithms");
        if (dsa == null) dsa = subjectMap.get("DSA");
        Subject os = subjectMap.get("Operating Systems");
        if (os == null) os = subjectMap.get("OS");

        User alex = students.get(0);
        if (java != null) {
            MockTest t = new MockTest();
            t.setUser(alex);
            t.setSubject(java);
            t.setTopic(topicMap.get("OOP Concepts"));
            t.setDifficulty(Difficulty.MEDIUM);
            t.setTotalQuestions(10);
            t.setDurationMinutes(15);
            t.setScore(8.0);
            t.setMaxScore(10.0);
            t.setPercentage(80.0);
            t.setAccuracy(80.0);
            t.setCorrectCount(8);
            t.setWrongCount(2);
            t.setSkippedCount(0);
            t.setTimeTakenSeconds(540);
            t.setStatus(MockTestStatus.COMPLETED);
            t.setCompletedAt(LocalDateTime.now().minusDays(1));
            mockTestRepository.save(t);
        }

        if (react != null) {
            MockTest t = new MockTest();
            t.setUser(alex);
            t.setSubject(react);
            t.setTopic(topicMap.get("React Hooks"));
            t.setDifficulty(Difficulty.EASY);
            t.setTotalQuestions(5);
            t.setDurationMinutes(10);
            t.setScore(5.0);
            t.setMaxScore(5.0);
            t.setPercentage(100.0);
            t.setAccuracy(100.0);
            t.setCorrectCount(5);
            t.setWrongCount(0);
            t.setSkippedCount(0);
            t.setTimeTakenSeconds(260);
            t.setStatus(MockTestStatus.COMPLETED);
            t.setCompletedAt(LocalDateTime.now().minusHours(8));
            mockTestRepository.save(t);
        }

        if (students.size() > 1 && spring != null) {
            User priya = students.get(1);
            MockTest t = new MockTest();
            t.setUser(priya);
            t.setSubject(spring);
            t.setTopic(topicMap.get("Spring MVC & REST"));
            t.setDifficulty(Difficulty.MEDIUM);
            t.setTotalQuestions(10);
            t.setDurationMinutes(15);
            t.setScore(8.5);
            t.setMaxScore(10.0);
            t.setPercentage(85.0);
            t.setAccuracy(85.0);
            t.setCorrectCount(9);
            t.setWrongCount(1);
            t.setSkippedCount(0);
            t.setTimeTakenSeconds(610);
            t.setStatus(MockTestStatus.COMPLETED);
            t.setCompletedAt(LocalDateTime.now().minusDays(2));
            mockTestRepository.save(t);
        }

        if (students.size() > 1 && mysql != null) {
            User priya = students.get(1);
            MockTest t = new MockTest();
            t.setUser(priya);
            t.setSubject(mysql);
            t.setTopic(topicMap.get("Transactions & ACID"));
            t.setDifficulty(Difficulty.HARD);
            t.setTotalQuestions(10);
            t.setDurationMinutes(20);
            t.setScore(9.0);
            t.setMaxScore(10.0);
            t.setPercentage(90.0);
            t.setAccuracy(90.0);
            t.setCorrectCount(9);
            t.setWrongCount(1);
            t.setSkippedCount(0);
            t.setTimeTakenSeconds(780);
            t.setStatus(MockTestStatus.COMPLETED);
            t.setCompletedAt(LocalDateTime.now().minusDays(3));
            mockTestRepository.save(t);
        }

        if (students.size() > 2 && dsa != null) {
            User rahul = students.get(2);
            MockTest t = new MockTest();
            t.setUser(rahul);
            t.setSubject(dsa);
            t.setTopic(topicMap.get("Dynamic Programming"));
            t.setDifficulty(Difficulty.HARD);
            t.setTotalQuestions(10);
            t.setDurationMinutes(25);
            t.setScore(9.5);
            t.setMaxScore(10.0);
            t.setPercentage(95.0);
            t.setAccuracy(95.0);
            t.setCorrectCount(10);
            t.setWrongCount(0);
            t.setSkippedCount(0);
            t.setTimeTakenSeconds(920);
            t.setStatus(MockTestStatus.COMPLETED);
            t.setCompletedAt(LocalDateTime.now().minusDays(1));
            mockTestRepository.save(t);
        }

        if (students.size() > 3 && os != null) {
            User ananya = students.get(3);
            MockTest t = new MockTest();
            t.setUser(ananya);
            t.setSubject(os);
            t.setTopic(topicMap.get("Processes & Threads"));
            t.setDifficulty(Difficulty.MEDIUM);
            t.setTotalQuestions(10);
            t.setDurationMinutes(15);
            t.setScore(7.5);
            t.setMaxScore(10.0);
            t.setPercentage(75.0);
            t.setAccuracy(75.0);
            t.setCorrectCount(8);
            t.setWrongCount(2);
            t.setSkippedCount(0);
            t.setTimeTakenSeconds(510);
            t.setStatus(MockTestStatus.COMPLETED);
            t.setCompletedAt(LocalDateTime.now().minusHours(14));
            mockTestRepository.save(t);
        }
    }

    private void createSubjectAndTopics(Map<String, Subject> subMap, Map<String, Topic> topMap) {
        // Java
        Subject java = new Subject("Java", "Core Java, OOP, Collections, Concurrency, JVM Internals and Modern Java 8-17 features.", "Coffee", "#f97316");
        java = subjectRepository.save(java);
        subMap.put("Java", java);
        topMap.put("Java_OOP", topicRepository.save(new Topic("OOP Concepts", "Classes, Inheritance, Polymorphism, Encapsulation, Abstraction", java)));
        topMap.put("Java_Collections", topicRepository.save(new Topic("Collections Framework", "List, Set, Map, Queue, ConcurrentHashMap, Sorting", java)));
        topMap.put("Java_Concurrency", topicRepository.save(new Topic("Multithreading & Concurrency", "Thread Lifecycle, Synchronization, Executors, Locks, Volatile", java)));
        topMap.put("Java_Exceptions", topicRepository.save(new Topic("Exception Handling", "Checked vs Unchecked, Try-with-resources, Custom Exceptions", java)));
        topMap.put("Java_Streams", topicRepository.save(new Topic("Streams & Lambdas", "Functional Interfaces, Map, Filter, Reduce, Collector", java)));

        // Spring Boot
        Subject spring = new Subject("Spring Boot", "Spring Core, IoC/DI, Spring MVC, REST APIs, Spring Security, JPA & Microservices.", "Leaf", "#10b981");
        spring = subjectRepository.save(spring);
        subMap.put("Spring Boot", spring);
        topMap.put("Spring_IoC", topicRepository.save(new Topic("IoC & Dependency Injection", "ApplicationContext, Bean Scopes, @Autowired, Component Scanning", spring)));
        topMap.put("Spring_MVC", topicRepository.save(new Topic("Spring MVC & REST", "Controllers, RequestMapping, ExceptionHandler, DTO Validation", spring)));
        topMap.put("Spring_Security", topicRepository.save(new Topic("Spring Security & JWT", "Filter Chains, Authentication, Authorization, Bearer Tokens", spring)));
        topMap.put("Spring_Data", topicRepository.save(new Topic("Spring Data JPA", "Repositories, Derived Queries, Entity Lifecycle, Lazy Loading", spring)));

        // React
        Subject react = new Subject("React", "Modern React, Functional Components, Hooks, State Management, Router and Performance.", "Atom", "#06b6d4");
        react = subjectRepository.save(react);
        subMap.put("React", react);
        topMap.put("React_Hooks", topicRepository.save(new Topic("React Hooks", "useState, useEffect, useMemo, useCallback, useRef, Custom Hooks", react)));
        topMap.put("React_DOM", topicRepository.save(new Topic("Virtual DOM & Rendering", "Reconciliation, Diffing Algorithm, Keys, Re-rendering", react)));
        topMap.put("React_State", topicRepository.save(new Topic("State Management", "Prop Drilling, Context API, Redux Toolkit, Zustand", react)));

        // JavaScript
        Subject js = new Subject("JavaScript", "ES6+, Event Loop, Promises, Closures, Prototypal Inheritance, Asynchronous JS.", "Code2", "#eab308");
        js = subjectRepository.save(js);
        subMap.put("JavaScript", js);
        topMap.put("JS_Async", topicRepository.save(new Topic("Async & Event Loop", "Microtasks, Macrotasks, Promises, async/await, Callbacks", js)));
        topMap.put("JS_Closures", topicRepository.save(new Topic("Closures & Scope", "Lexical Scoping, Hoisting, Execution Context, 'this' keyword", js)));

        // MySQL
        Subject mysql = new Subject("MySQL & SQL", "Relational Database Design, SQL Queries, Joins, Indexing, Transactions and Optimization.", "Database", "#3b82f6");
        mysql = subjectRepository.save(mysql);
        subMap.put("MySQL", mysql);
        topMap.put("SQL_Queries", topicRepository.save(new Topic("Queries & Joins", "Inner, Left, Right, Full Outer Joins, GROUP BY, HAVING", mysql)));
        topMap.put("SQL_Indexes", topicRepository.save(new Topic("Indexing & Performance", "B-Tree, Clustered vs Non-clustered, Query Execution Plans", mysql)));
        topMap.put("SQL_ACID", topicRepository.save(new Topic("Transactions & ACID", "Atomicity, Consistency, Isolation Levels, Durability", mysql)));

        // DSA
        Subject dsa = new Subject("Data Structures & Algorithms", "Arrays, Strings, Linked Lists, Trees, Graphs, Dynamic Programming and System Optimization.", "Binary", "#8b5cf6");
        dsa = subjectRepository.save(dsa);
        subMap.put("DSA", dsa);
        topMap.put("DSA_Arrays", topicRepository.save(new Topic("Arrays & Hashing", "Two Pointers, Sliding Window, Prefix Sum, Hash Maps", dsa)));
        topMap.put("DSA_Trees", topicRepository.save(new Topic("Trees & BST", "Traversals, Balanced Trees, LCA, Tree Depth", dsa)));
        topMap.put("DSA_DP", topicRepository.save(new Topic("Dynamic Programming", "Memoization, Tabulation, Knapsack, Longest Subsequences", dsa)));

        // DBMS
        Subject dbms = new Subject("DBMS", "Relational Models, Normalization, Concurrency Control, Storage Architectures, Relational Algebra.", "Server", "#6366f1");
        dbms = subjectRepository.save(dbms);
        subMap.put("DBMS", dbms);
        topMap.put("DBMS_Norm", topicRepository.save(new Topic("Normalization", "1NF, 2NF, 3NF, BCNF, Functional Dependencies", dbms)));
        topMap.put("DBMS_Concurrency", topicRepository.save(new Topic("Concurrency & Locks", "2-Phase Locking, Timestamp Ordering, Deadlocks", dbms)));

        // Operating Systems
        Subject os = new Subject("Operating Systems", "Process Management, Threads, Deadlocks, Memory Management, Virtual Memory and File Systems.", "Cpu", "#ec4899");
        os = subjectRepository.save(os);
        subMap.put("OS", os);
        topMap.put("OS_Processes", topicRepository.save(new Topic("Processes & Threads", "Context Switching, IPC, Multithreading, Scheduling Algorithms", os)));
        topMap.put("OS_Memory", topicRepository.save(new Topic("Memory & Paging", "Paging, Segmentation, TLB, Page Replacement Algorithms", os)));

        // Computer Networks
        Subject cn = new Subject("Computer Networks", "OSI & TCP/IP Reference Models, HTTP/HTTPS, DNS, Transport Protocols, IP Addressing.", "Network", "#14b8a6");
        cn = subjectRepository.save(cn);
        subMap.put("CN", cn);
        topMap.put("CN_Layers", topicRepository.save(new Topic("OSI & TCP/IP Layers", "Physical to Application Layers, Packet Encapsulation", cn)));
        topMap.put("CN_Protocols", topicRepository.save(new Topic("TCP vs UDP & Protocols", "Three-Way Handshake, Flow Control, Congestion Control, DNS", cn)));

        // Aptitude
        Subject apt = new Subject("Aptitude & Reasoning", "Quantitative Aptitude, Time & Work, Speed & Distance, Probability and Logical Puzzles.", "Brain", "#f43f5e");
        apt = subjectRepository.save(apt);
        subMap.put("Aptitude", apt);
        topMap.put("Apt_Quant", topicRepository.save(new Topic("Quantitative Math", "Percentages, Ratios, Time & Work, Speed & Distance", apt)));
    }

    private void seedQuestions(Map<String, Subject> subMap, Map<String, Topic> topMap) {
        List<Question> questions = new ArrayList<>();

        // JAVA QUESTIONS
        questions.add(new Question(
                subMap.get("Java"), topMap.get("Java_OOP"), Difficulty.EASY,
                "Which keyword is used for inheritance between classes in Java?",
                null,
                "implements", "extends", "inherits", "super",
                "B",
                "The 'extends' keyword is used when one Java class inherits another class. 'implements' is used when a class implements an interface.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("Java"), topMap.get("Java_OOP"), Difficulty.MEDIUM,
                "What is the output of the following Java snippet?",
                "class Parent {\n    void print() { System.out.print(\"Parent \"); }\n}\nclass Child extends Parent {\n    void print() { System.out.print(\"Child \"); }\n}\npublic class Main {\n    public static void main(String[] args) {\n        Parent obj = new Child();\n        obj.print();\n    }\n}",
                "Parent", "Child", "Compilation Error", "Runtime Exception",
                "B",
                "In Java, method calls are resolved at runtime based on the actual object instance (Runtime Polymorphism / Dynamic Method Dispatch). Since 'obj' refers to an instance of Child, Child's print() is invoked.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("Java"), topMap.get("Java_Collections"), Difficulty.MEDIUM,
                "What is the time complexity of searching for an element by key in a well-distributed java.util.HashMap in Java 8?",
                null,
                "O(n)", "O(log n)", "O(1) average, O(log n) worst case", "O(n^2)",
                "C",
                "In Java 8, when hash collisions in a single bucket exceed TREEIFY_THRESHOLD (8), the linked list converts into a balanced Red-Black Tree (TreeNode), reducing worst-case lookup from O(n) to O(log n). Average case remains O(1).",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("Java"), topMap.get("Java_Concurrency"), Difficulty.HARD,
                "What guarantees does the 'volatile' keyword provide in Java?",
                null,
                "Atomicity and Mutual Exclusion",
                "Visibility across threads and prevents instruction reordering (happens-before relationship)",
                "Locks the object monitor exclusively",
                "Creates an immutable copy in thread-local cache",
                "B",
                "'volatile' ensures that reads and writes are made directly to main memory, guaranteeing visibility and ordering through memory barriers. However, it does NOT provide atomicity for compound actions like count++.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("Java"), topMap.get("Java_Exceptions"), Difficulty.MEDIUM,
                "Which of the following exceptions is an UNCHECKED exception in Java?",
                null,
                "java.io.IOException", "java.sql.SQLException", "java.lang.NullPointerException", "java.lang.ClassNotFoundException",
                "C",
                "NullPointerException inherits from RuntimeException, which makes it an unchecked exception. All subclasses of RuntimeException and Error are unchecked.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("Java"), topMap.get("Java_Streams"), Difficulty.MEDIUM,
                "What does the following Java Stream pipeline output?",
                "List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);\nint result = numbers.stream()\n                    .filter(n -> n % 2 == 0)\n                    .map(n -> n * 2)\n                    .reduce(0, Integer::sum);\nSystem.out.println(result);",
                "12", "6", "10", "14",
                "A",
                "The filter selects even numbers [2, 4]. The map doubles each: [4, 8]. The reduce sums them: 4 + 8 = 12.",
                1.0, 0.25
        ));

        // SPRING BOOT QUESTIONS
        questions.add(new Question(
                subMap.get("Spring Boot"), topMap.get("Spring_IoC"), Difficulty.EASY,
                "What does the @SpringBootApplication annotation combine?",
                null,
                "@Configuration, @EnableAutoConfiguration, and @ComponentScan",
                "@Controller, @Service, and @Repository",
                "@Entity, @Table, and @Id",
                "@Component, @Bean, and @Autowired",
                "A",
                "@SpringBootApplication is a convenience annotation that aggregates @SpringBootConfiguration (which is @Configuration), @EnableAutoConfiguration, and @ComponentScan with default attributes.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("Spring Boot"), topMap.get("Spring_IoC"), Difficulty.MEDIUM,
                "What is the default bean scope in Spring Framework?",
                null,
                "Prototype", "Singleton", "Request", "Session",
                "B",
                "In Spring, beans are 'Singleton' by default, meaning one shared instance of the bean is created per Spring ApplicationContext.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("Spring Boot"), topMap.get("Spring_MVC"), Difficulty.MEDIUM,
                "Which HTTP status code is most appropriate for a REST endpoint when a resource is successfully created?",
                null,
                "200 OK", "201 Created", "204 No Content", "202 Accepted",
                "B",
                "HTTP 201 Created indicates that the request has succeeded and led to the creation of a new resource, commonly accompanied by a Location header referencing the new entity.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("Spring Boot"), topMap.get("Spring_Security"), Difficulty.HARD,
                "Where should the custom JwtAuthenticationFilter typically be placed within the Spring Security filter chain?",
                null,
                "Before UsernamePasswordAuthenticationFilter",
                "After BasicAuthenticationFilter only",
                "At the very end of the SecurityFilterChain",
                "Before ChannelProcessingFilter",
                "A",
                "In token-based stateless architectures, JwtAuthenticationFilter is commonly positioned before UsernamePasswordAuthenticationFilter (http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)) so incoming JWTs are validated and populated into the SecurityContextHolder before form login processing.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("Spring Boot"), topMap.get("Spring_Data"), Difficulty.MEDIUM,
                "How does Spring Data JPA resolve the N+1 select query problem?",
                null,
                "By disabling caching completely",
                "Using JOIN FETCH in JPQL or @EntityGraph",
                "By converting all relationships to EAGER fetching",
                "By executing native queries only",
                "B",
                "The N+1 problem occurs when fetching an entity triggers N additional SQL queries for related child entities. It is resolved using 'JOIN FETCH' in JPQL or declarative '@EntityGraph' to load relationships in a single SQL query.",
                1.0, 0.25
        ));

        // REACT QUESTIONS
        questions.add(new Question(
                subMap.get("React"), topMap.get("React_Hooks"), Difficulty.EASY,
                "Which React hook is used to perform side effects such as data fetching or subscriptions?",
                null,
                "useState", "useEffect", "useMemo", "useReducer",
                "B",
                "useEffect lets you synchronize a component with an external system and perform side-effects such as network requests, DOM mutations, and event listener subscriptions.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("React"), topMap.get("React_Hooks"), Difficulty.MEDIUM,
                "What is the purpose of the dependency array in useEffect?",
                null,
                "Specifies CSS classes applied to the component",
                "Controls when the effect re-runs: only re-executes if any value in the array changes between renders",
                "Allocates memory for state variables",
                "Enforces strict type checking in TypeScript",
                "B",
                "If the dependency array is empty [], the effect runs once on mount. If dependencies are specified [a, b], the effect re-runs whenever 'a' or 'b' changes. If omitted entirely, it runs after every single render.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("React"), topMap.get("React_DOM"), Difficulty.MEDIUM,
                "Why should you avoid using the array index as a 'key' prop when rendering lists in React?",
                null,
                "Keys must strictly be numbers, not strings",
                "It causes syntax errors in JSX",
                "It can cause UI state glitches, incorrect input focus, and sub-optimal reconciliation performance when items are reordered or deleted",
                "React ignores keys completely if they are numeric",
                "C",
                "React's reconciliation algorithm uses keys to identify which items have changed, added, or removed. Using array indices can lead to state bleeding across rows when items are reordered, inserted, or removed.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("React"), topMap.get("React_Hooks"), Difficulty.HARD,
                "What is the core difference between useMemo and useCallback?",
                null,
                "useMemo memoizes a computed value, while useCallback memoizes a callback function instance",
                "useCallback is synchronous while useMemo is asynchronous",
                "useMemo is deprecated in React 18 in favor of useCallback",
                "useCallback cannot take dependency arrays",
                "A",
                "useMemo returns the cached result of a calculation fn: useMemo(() => compute(), [deps]). useCallback returns a memoized version of the function itself: useCallback(fn, [deps]). In fact, useCallback(fn, deps) is equivalent to useMemo(() => fn, deps).",
                1.0, 0.25
        ));

        // JAVASCRIPT QUESTIONS
        questions.add(new Question(
                subMap.get("JavaScript"), topMap.get("JS_Async"), Difficulty.MEDIUM,
                "What is the printed order of the following JavaScript snippet?",
                "console.log('1');\nsetTimeout(() => console.log('2'), 0);\nPromise.resolve().then(() => console.log('3'));\nconsole.log('4');",
                "1, 2, 3, 4", "1, 4, 2, 3", "1, 4, 3, 2", "3, 1, 4, 2",
                "C",
                "Synchronous code runs first ('1', '4'). Next, the microtask queue (Promises) is drained before macrotasks, printing '3'. Finally, setTimeout's callback from the macrotask/task queue executes, printing '2'. Thus: 1, 4, 3, 2.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("JavaScript"), topMap.get("JS_Closures"), Difficulty.MEDIUM,
                "What is a closure in JavaScript?",
                null,
                "A method to close browser tabs programmatically",
                "A function bundled together with references to its surrounding lexical state (lexical environment)",
                "A syntax error caused by unmatched curly braces",
                "An immutable frozen object created with Object.freeze()",
                "B",
                "A closure is the combination of a function bundled together (enclosed) with references to its surrounding state (the lexical environment). In JavaScript, closures give inner functions access to outer function scope even after the outer function has returned.",
                1.0, 0.25
        ));

        // MYSQL & SQL QUESTIONS
        questions.add(new Question(
                subMap.get("MySQL"), topMap.get("SQL_Indexes"), Difficulty.MEDIUM,
                "What is the key difference between a Clustered Index and a Non-Clustered Index in MySQL InnoDB?",
                null,
                "Clustered indexes are slower than non-clustered indexes",
                "The clustered index determines the physical order of data rows in the table; each table can have only ONE clustered index (the Primary Key)",
                "Non-clustered indexes store table rows directly inside index leaves",
                "MySQL InnoDB does not support clustered indexes",
                "B",
                "In InnoDB, the primary key forms the Clustered Index, and the table data itself is stored in the leaf pages of this B+ Tree. Secondary (non-clustered) indexes store the indexed columns and point to the primary key value.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("MySQL"), topMap.get("SQL_ACID"), Difficulty.HARD,
                "Which transaction isolation level prevents Dirty Reads and Non-Repeatable Reads, but may still permit Phantom Reads under the ANSI SQL standard?",
                null,
                "Read Uncommitted", "Read Committed", "Repeatable Read", "Serializable",
                "C",
                "Repeatable Read prevents Dirty Reads (reading uncommitted changes) and Non-Repeatable Reads (re-reading a modified row). ANSI SQL specifies that Repeatable Read may allow Phantom Reads (new rows inserted by concurrent transactions), although InnoDB prevents phantom reads even in Repeatable Read using Next-Key Locks.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("MySQL"), topMap.get("SQL_Queries"), Difficulty.EASY,
                "Which SQL clause is used to filter records AFTER an aggregation operation (such as SUM, COUNT, AVG)?",
                null,
                "WHERE", "HAVING", "GROUP BY", "ORDER BY",
                "B",
                "WHERE filters rows before grouping/aggregations take place. HAVING filters groups after aggregate calculations have occurred.",
                1.0, 0.25
        ));

        // DATA STRUCTURES & ALGORITHMS QUESTIONS
        questions.add(new Question(
                subMap.get("DSA"), topMap.get("DSA_Arrays"), Difficulty.EASY,
                "What is the average time complexity of looking up a value in a Hash Map?",
                null,
                "O(n)", "O(log n)", "O(1)", "O(n log n)",
                "C",
                "Due to direct array indexing using hash functions, looking up a key-value pair in a properly dimensioned hash map is O(1) constant time on average.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("DSA"), topMap.get("DSA_Trees"), Difficulty.MEDIUM,
                "Which tree traversal visits the nodes in ascending order in a Binary Search Tree (BST)?",
                null,
                "Pre-order Traversal (Root, Left, Right)",
                "In-order Traversal (Left, Root, Right)",
                "Post-order Traversal (Left, Right, Root)",
                "Level-order Traversal (BFS)",
                "B",
                "In a BST, all values in the left subtree are smaller than the root, and all values in the right subtree are greater. An In-order traversal (Left -> Root -> Right) visits nodes in strictly sorted ascending order.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("DSA"), topMap.get("DSA_DP"), Difficulty.HARD,
                "What is the time complexity of solving the 0/1 Knapsack Problem with N items and maximum weight capacity W using Dynamic Programming?",
                null,
                "O(2^N)", "O(N * W)", "O(N log W)", "O(N + W)",
                "B",
                "The dynamic programming solution builds a 2D table of size (N+1) x (W+1), computing each state in O(1) time. The overall time and space complexity is pseudo-polynomial: O(N * W).",
                1.0, 0.25
        ));

        // DBMS QUESTIONS
        questions.add(new Question(
                subMap.get("DBMS"), topMap.get("DBMS_Norm"), Difficulty.MEDIUM,
                "A relation is in Third Normal Form (3NF) if it is in 2NF and has no:",
                null,
                "Partial functional dependencies",
                "Transitive functional dependencies",
                "Multi-valued dependencies",
                "Primary key",
                "B",
                "2NF removes partial functional dependencies (where a non-prime attribute depends on part of a composite candidate key). 3NF additionally eliminates transitive dependencies (where non-prime attributes determine other non-prime attributes: X -> Y -> Z).",
                1.0, 0.25
        ));

        // OPERATING SYSTEMS QUESTIONS
        questions.add(new Question(
                subMap.get("OS"), topMap.get("OS_Processes"), Difficulty.MEDIUM,
                "Which of the following is NOT one of the Coffman conditions required for a Deadlock to occur?",
                null,
                "Mutual Exclusion",
                "Hold and Wait",
                "Preemption Allowed",
                "Circular Wait",
                "C",
                "The four Coffman conditions are: 1. Mutual Exclusion, 2. Hold and Wait, 3. NO Preemption, and 4. Circular Wait. If preemption is allowed, resources can be reclaimed and deadlocks can be resolved.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("OS"), topMap.get("OS_Memory"), Difficulty.MEDIUM,
                "What phenomenon occurs when the operating system spends more time swapping pages into and out of memory than executing actual process instructions?",
                null,
                "Fragmentation", "Thrashing", "Deadlock", "Starvation",
                "B",
                "Thrashing happens when the working set of active processes exceeds physical RAM capacity, causing page fault frequency to spike and CPU utilization to plummet as the disk thrash occurs.",
                1.0, 0.25
        ));

        // COMPUTER NETWORKS QUESTIONS
        questions.add(new Question(
                subMap.get("CN"), topMap.get("CN_Protocols"), Difficulty.EASY,
                "How many packets are exchanged in the standard TCP connection establishment handshake?",
                null,
                "2 packets (SYN, ACK)",
                "3 packets (SYN, SYN-ACK, ACK)",
                "4 packets (FIN, ACK, FIN, ACK)",
                "1 packet (INIT)",
                "B",
                "TCP uses a 3-way handshake: Client sends SYN -> Server responds with SYN-ACK -> Client sends ACK. Connection is then ESTABLISHED.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("CN"), topMap.get("CN_Layers"), Difficulty.MEDIUM,
                "Which layer of the OSI model is responsible for end-to-end communication, segmentation, and flow control?",
                null,
                "Network Layer", "Data Link Layer", "Transport Layer", "Session Layer",
                "C",
                "The Transport Layer (Layer 4, e.g. TCP and UDP) provides reliable host-to-host or end-to-end communication, error recovery, flow control, and data segmentation.",
                1.0, 0.25
        ));

        // APTITUDE QUESTIONS
        questions.add(new Question(
                subMap.get("Aptitude"), topMap.get("Apt_Quant"), Difficulty.EASY,
                "If Person A can complete a job in 10 days and Person B can complete the same job in 15 days, how many days will they take working together?",
                null,
                "5 days", "6 days", "7.5 days", "8 days",
                "B",
                "In 1 day, A does 1/10 and B does 1/15 of the work. Together in 1 day: (1/10 + 1/15) = (3 + 2)/30 = 5/30 = 1/6. Therefore, the whole work is finished in 1 / (1/6) = 6 days.",
                1.0, 0.25
        ));

        questions.add(new Question(
                subMap.get("Aptitude"), topMap.get("Apt_Quant"), Difficulty.MEDIUM,
                "A train 180 meters long crosses a pole in 9 seconds. What is the speed of the train in km/h?",
                null,
                "72 km/h", "60 km/h", "54 km/h", "90 km/h",
                "A",
                "Speed in m/s = Distance / Time = 180 / 9 = 20 m/s. To convert m/s to km/h, multiply by 18/5: 20 * (18/5) = 4 * 18 = 72 km/h.",
                1.0, 0.25
        ));

        questionRepository.saveAll(questions);
    }

    private void seedSubjectNotes(Map<String, Subject> subjectMap) {
        if (subjectMap == null || subjectMap.isEmpty()) return;
        List<SubjectNote> notes = new ArrayList<>();

        Subject java = subjectMap.get("Java");
        if (java != null) {
            notes.add(new SubjectNote("Oracle Java Official Documentation & Tutorials", "https://docs.oracle.com/javase/tutorial/", "Core language specifications, OOP concepts, generics, and API reference.", java));
            notes.add(new SubjectNote("Java Concurrency & Multithreading Revision Guide", "https://www.geeksforgeeks.org/multithreading-in-java/", "Thread lifecycle, executors, locks, and synchronized blocks cheat sheet.", java));
            notes.add(new SubjectNote("Baeldung Java Streams & Lambdas Deep Dive", "https://www.baeldung.com/java-8-streams", "Functional interfaces, parallel streams, and collectors guide.", java));
        }

        Subject spring = subjectMap.get("Spring Boot");
        if (spring != null) {
            notes.add(new SubjectNote("Spring Boot Official Reference Documentation", "https://spring.io/projects/spring-boot", "Production-grade enterprise Spring architectures, actuators, and starters.", spring));
            notes.add(new SubjectNote("Spring Security & JWT Authentication Guide", "https://spring.io/guides/tutorials/rest/", "Stateless API protection, filter chains, and token validation notes.", spring));
        }

        Subject react = subjectMap.get("React");
        if (react != null) {
            notes.add(new SubjectNote("React.dev Official Modern Documentation & Hooks", "https://react.dev/", "Deep-dive into useState, useEffect, useCallback, and React 19 paradigms.", react));
            notes.add(new SubjectNote("React Reconciliation & Fiber Architecture Notes", "https://react.dev/reference/react", "Diffing algorithm, fiber reconciliation, and performance optimization.", react));
        }

        Subject mysql = subjectMap.get("MySQL & SQL");
        if (mysql == null) mysql = subjectMap.get("MySQL");
        if (mysql != null) {
            notes.add(new SubjectNote("MySQL 8.0 Reference Manual & Query Optimization", "https://dev.mysql.com/doc/refman/8.0/en/", "Relational schema designs, clustered indexing, and ACID guarantees.", mysql));
            notes.add(new SubjectNote("Use The Index, Luke: SQL Indexing Cheat Sheet", "https://use-the-index-luke.com/", "B-Tree indexes, query execution plans, and join performance tuning.", mysql));
        }

        Subject dsa = subjectMap.get("Data Structures & Algorithms");
        if (dsa == null) dsa = subjectMap.get("DSA");
        if (dsa != null) {
            notes.add(new SubjectNote("VisuAlgo: Interactive DSA Algorithms Visualizer", "https://visualgo.net/", "Animated algorithmic walkthroughs for trees, graphs, sorting, and DP.", dsa));
            notes.add(new SubjectNote("NeetCode Technical Interview Roadmap & Patterns", "https://neetcode.io/roadmap", "Curated patterns for binary search, two pointers, sliding window, and graphs.", dsa));
        }

        if (!notes.isEmpty()) {
            subjectNoteRepository.saveAllAndFlush(notes);
            logger.info("Seeded {} subject note links across subjects.", notes.size());
        }
    }
}
