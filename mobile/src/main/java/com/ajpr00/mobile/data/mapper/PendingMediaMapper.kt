import com.ajpr00.mobile.data.datasource.local.db.entity.PendingMediaEntity
import com.ajpr00.core.domain.model.PendingMedia

fun PendingMediaEntity.toDomain() = PendingMedia(
    id = id,
    filePath = filePath,
    thumbnailPath = thumbnailPath,
    type = type,
    status = status,
    createdAt = createdAt,
    retries = retries
)

fun PendingMedia.toEntity() = PendingMediaEntity(
    id = id,
    filePath = filePath,
    thumbnailPath = thumbnailPath,
    type = type,
    status = status,
    createdAt = createdAt,
    retries = retries
)
