package com.mooddiary.diary.application.photo;

import java.util.List;

public record UploadDiaryEntryPhotosCommand(
        List<PhotoUploadFileCommand> files
) {
}

