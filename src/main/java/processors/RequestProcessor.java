package processors;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface RequestProcessor<T, R> {
    R process(T request) throws ExecutionException, InterruptedException, IOException;
}
