package processors;

public interface RequestProcessor<T> {
    void process(T request);
}
