import com.github.javafaker.Faker;

import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class DataGenerator {
    final private Faker faker;
    final private String[] themes;
    final private Random random;

    public DataGenerator() {
        faker = new Faker(new Locale("de"));
        themes = new String[5];
        themes[0] = "Verwaltung";
        themes[1] = "Verkehr";
        themes[2] = "Agrar";
        themes[3] = "Forschung";
        themes[4] = "Gesundheit";
        random = new Random();
    }

    public String getId() {
        return UUID.randomUUID().toString();
    }
    public String getDescription() {
        return faker.yoda().quote();
    }

    public String getName() {
        return faker.name().fullName();
    }

    public String getOrgName(){
        return faker.company().name();
    }

    public String getMail() {
        return faker.internet().emailAddress();
    }

    public String getOrgMail() {
        return faker.internet().emailAddress();
    }

    public String getTheme() {
        int n = themes.length - 1;
        int i = Math.abs(random.nextInt()) % n;
        return themes[i];
    }
}
