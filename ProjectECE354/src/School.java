import java.util.ArrayList;
import java.util.List;

public class School {
    private String schoolName;
    private List<Course> courses;

    public School(String schoolName) {
        this.schoolName = schoolName;
        this.courses = new ArrayList<>();
    }

    public void addCourse(Course course) {
        courses.add(course);
    }

    public String getSchoolName() {
        return schoolName;
    }

    public List<Course> getCourses() {
        return new ArrayList<>(courses);
    }
}
