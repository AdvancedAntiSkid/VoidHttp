package net.voidhttp.request.form;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@RequiredArgsConstructor
@ToString
public class RequestMultipartForm implements MultipartForm {
    private final List<FormEntry> entries;

    @Override
    public List<FormEntry> entries() {
        return entries;
    }
}
