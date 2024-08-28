/**
 * @author 缪林辉
 * @date 2024/8/28 17:52
 * @DESCRIPTION
 */
public interface ResumeSub {
    void login();
    ResumeSub city(String[]cities);
    ResumeSub salary(int min,int max);
    ResumeSub experience(String[]experiences);
    ResumeSub jobType(String[]types);
    ResumeSub page(int pageNum);
    void startResumeSubmission();
}
