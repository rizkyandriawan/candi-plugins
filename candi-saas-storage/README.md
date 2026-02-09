# candi-saas-storage

S3/Minio file upload plugin for Candi pages with automatic multipart processing.

## Installation

**Maven**

```xml
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-saas-storage</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

**Gradle**

```groovy
implementation 'io.candi:candi-saas-storage:0.1.0-SNAPSHOT'
```

## Configuration

Add storage properties to `application.yml`. The plugin activates when `candi.storage.access-key` is set.

**AWS S3**

```yaml
candi:
  storage:
    provider: s3
    region: us-east-1
    access-key: AKIAIOSFODNN7EXAMPLE
    secret-key: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
    default-bucket: uploads
    cdn-url: https://cdn.example.com    # optional
```

**Minio**

```yaml
candi:
  storage:
    provider: minio
    endpoint: http://localhost:9000
    region: us-east-1
    access-key: minioadmin
    secret-key: minioadmin
    default-bucket: uploads
```

All properties under `candi.storage.*`:

| Property | Default | Description |
|----------|---------|-------------|
| `provider` | `s3` | Storage provider: `s3` or `minio` |
| `endpoint` | (empty) | Custom endpoint URL. Required for Minio. |
| `region` | `us-east-1` | AWS region |
| `access-key` | (empty) | Access key ID (triggers auto-configuration) |
| `secret-key` | (empty) | Secret access key |
| `default-bucket` | `uploads` | Default bucket name. Auto-created if missing. |
| `cdn-url` | (empty) | CDN base URL for public file URLs |

## @Upload Annotation

Annotate a `String` or `StorageResult` field on your page class. The `UploadInterceptor` automatically processes multipart uploads **before** `onPost()` runs. When the request contains a file matching the field name, the file is validated, uploaded to storage, and the field is set to the resulting URL (for `String` fields) or the full `StorageResult` record.

```java
@Upload(
    bucket = "",                         // override default bucket
    path = "avatars/",                   // path prefix in bucket
    maxSize = 10485760,                  // max file size in bytes (default: 10MB)
    allowedTypes = {"image/png", "image/jpeg"},  // MIME filter (empty = all)
    publicRead = false                   // set public-read ACL
)
private String avatarUrl;
```

Field types:

- `String` -- receives the accessible URL of the uploaded file.
- `StorageResult` -- receives the full result record with `key`, `url`, `bucket`, `size`, and `contentType`.

## StorageService API

Inject `StorageService` for programmatic file operations.

```java
@Autowired
private StorageService storageService;
```

| Method | Signature | Description |
|--------|-----------|-------------|
| `upload` | `upload(MultipartFile file, UploadOptions options)` | Upload a multipart file. Returns `StorageResult`. |
| `upload` | `upload(InputStream input, String filename, String contentType, UploadOptions options)` | Upload from an input stream. |
| `download` | `download(String key)` | Download from the default bucket. Returns `InputStream`. |
| `download` | `download(String key, String bucket)` | Download from a specific bucket. |
| `delete` | `delete(String key)` | Delete from the default bucket. |
| `delete` | `delete(String key, String bucket)` | Delete from a specific bucket. |
| `getUrl` | `getUrl(String key)` | Get the URL for a file. Uses CDN URL if configured. |
| `getPresignedUrl` | `getPresignedUrl(String key, Duration expiry)` | Generate a temporary presigned URL for a private file. |
| `getPresignedUrl` | `getPresignedUrl(String key, String bucket, Duration expiry)` | Presigned URL for a specific bucket. |
| `exists` | `exists(String key)` | Check if a file exists in the default bucket. |
| `exists` | `exists(String key, String bucket)` | Check if a file exists in a specific bucket. |

## Upload Widget

The plugin provides a built-in `cnd-upload` widget that renders a file input with drag-and-drop support and optional image preview.

```
{{ widget "cnd-upload" name="avatar" label="Profile Photo" accept="image/*" maxSize="5MB" preview=true }}
```

Widget parameters:

| Parameter | Required | Description |
|-----------|----------|-------------|
| `name` | yes | Form field name (must match the `@Upload` field name) |
| `label` | no | Display label above the input |
| `accept` | no | MIME type filter (e.g., `image/*`, `image/png,image/jpeg`) |
| `maxSize` | no | Human-readable max size hint (e.g., `10MB`, `500KB`) |
| `multiple` | no | Allow multiple files (boolean) |
| `preview` | no | Show image preview for selected files (boolean) |
| `currentUrl` | no | URL of the currently uploaded file (for edit forms) |
| `class` | no | Additional CSS classes |

The widget renders a styled dropzone with inline drag-and-drop JavaScript and scoped CSS. No external dependencies required.

## How Upload Processing Works

1. A user submits a form with `enctype="multipart/form-data"`.
2. The `UploadInterceptor` runs in `preHandle`, before the page lifecycle.
3. It checks if the request is multipart and the page has `@Upload`-annotated fields.
4. For each `@Upload` field with a matching file in the request:
   - Validates file size against `maxSize`.
   - Validates content type against `allowedTypes`.
   - Uploads the file to S3/Minio via `StorageService`.
   - Sets the field value to the URL (`String`) or the full `StorageResult`.
5. The page's `onPost()` method runs with the field already populated.

## Complete Example

A profile page with avatar upload:

```
import candi.annotation.Page;
import candi.annotation.Post;
import candi.saas.storage.Upload;
import candi.saas.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;

@Page(value = "/profile", layout = "base")
public class ProfilePage {

    @Autowired
    private StorageService storageService;

    @Autowired
    private UserRepository userRepository;

    @Upload(path = "avatars/", maxSize = 5242880, allowedTypes = {"image/png", "image/jpeg", "image/webp"}, publicRead = true)
    private String avatar;

    private User user;
    private String message;

    public void onGet() {
        user = getCurrentUser();
        avatar = user.getAvatarUrl();
    }

    @Post
    public String saveProfile() {
        user = getCurrentUser();
        // avatar is already populated by UploadInterceptor
        if (avatar != null && !avatar.isEmpty()) {
            // Delete old avatar if it exists
            String oldKey = user.getAvatarKey();
            if (oldKey != null) {
                storageService.delete(oldKey);
            }
            user.setAvatarUrl(avatar);
        }
        userRepository.save(user);
        message = "Profile updated.";
        return null;
    }
}
<template>
<div class="profile-form">
    <h1>Edit Profile</h1>

    {{ if message }}
        <div class="alert">{{ message }}</div>
    {{ end }}

    <form method="post" enctype="multipart/form-data">
        {{ widget "cnd-upload" name="avatar" label="Profile Photo" accept="image/*" maxSize="5MB" preview=true currentUrl=avatar }}

        <button type="submit">Save</button>
    </form>
</div>
</template>
```
