package com.example.social

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import com.example.Supabase
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextGray
import io.github.jan.supabase.auth.auth
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.launch

@Composable
fun WhatsOnYourMindSection(onNavigateToCreatePost: () -> Unit = {}) {
    var isUserLoggedIn by remember { mutableStateOf(false) }
    var currentUserName by remember { mutableStateOf("User") }
    var currentUserAvatar by remember { mutableStateOf<String?>(null) }
    
    val auth = remember { Supabase.client.auth }

    LaunchedEffect(Unit) {
        val user = auth.currentUserOrNull()
        isUserLoggedIn = user != null
        if (user != null) {
            currentUserName = user.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: "User"
            currentUserAvatar = user.userMetadata?.get("avatar_url")?.toString()?.replace("\"", "")
        }
    }

    if (!isUserLoggedIn) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color.LightGray.copy(alpha=0.5f), RoundedCornerShape(12.dp))
            .clickable { onNavigateToCreatePost() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Profile Logo Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(currentUserName.take(1).uppercase(), color = PrimaryGreen, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                "What's on your mind?", 
                color = TextGray, 
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha=0.5f))
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Image, contentDescription = "Photo", tint = Color(0xFF4CAF50))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Photo", color = TextDark, fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VideoLibrary, contentDescription = "Video", tint = Color(0xFFF44336))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Video", color = TextDark, fontSize = 14.sp)
            }
        }
    }
    
    // Video Feed below What's on your mind
    VideoFeedSection()
}

@Composable
fun VideoFeedSection() {
    val globalPosts by com.example.social.GlobalPostState.posts.collectAsState()
    var fetchedPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            // First fetch from Supabase
            val posts = com.example.Supabase.client.postgrest["posts"]
                .select().decodeList<Post>()
            fetchedPosts = posts.sortedByDescending { it.createdAt }
            com.example.social.GlobalPostState.setPosts(fetchedPosts)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to fetch from existing backend if available, though Supabase is preferred
        } finally {
            isLoading = false
        }
    }

    val displayPosts = globalPosts.ifEmpty { fetchedPosts }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text("Shorts Feed & Recent Posts", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
        Spacer(modifier = Modifier.height(12.dp))
        
        if (isLoading && displayPosts.isEmpty()) {
            CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (displayPosts.isEmpty()) {
            Text("No posts available right now.", color = TextGray, fontSize = 14.sp)
        } else {
            displayPosts.forEach { post ->
                VideoPostCard(post)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun VideoPostCard(post: Post) {
    var isLiked by remember { mutableStateOf(post.isLikedByMe) }
    var isSubscribed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color.LightGray.copy(alpha=0.5f), RoundedCornerShape(12.dp))
            .padding(bottom = 16.dp)
    ) {
        // User Header
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(post.userName.firstOrNull()?.toString()?.uppercase() ?: "U", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(post.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                Text("Just now", fontSize = 12.sp, color = TextGray)
            }
            TextButton(
                onClick = { isSubscribed = !isSubscribed },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isSubscribed) TextGray else PrimaryGreen
                )
            ) {
                Text(if (isSubscribed) "Subscribed" else "Subscribe", fontWeight = FontWeight.Bold)
            }
        }
        
        val isImage = post.mediaType == "photo" || post.mediaUrl.endsWith(".jpg", ignoreCase = true) || 
                      post.mediaUrl.endsWith(".jpeg", ignoreCase = true) ||
                      post.mediaUrl.endsWith(".png", ignoreCase = true)
                      
        if (isImage) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                coil.compose.AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
        } else {
            // Video Player using ExoPlayer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (post.mediaUrl.isNotEmpty()) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val exoPlayer = remember {
                        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                            setMediaItem(androidx.media3.common.MediaItem.fromUri(post.mediaUrl))
                            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
                            playWhenReady = true
                            prepare()
                        }
                    }
                    
                    DisposableEffect(exoPlayer) {
                        onDispose { exoPlayer.release() }
                    }
                    
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            androidx.media3.ui.PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.VideoLibrary, contentDescription = "Play Video", tint = Color.White.copy(alpha=0.5f), modifier = Modifier.size(64.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Post Title/Caption
        Text(
            text = post.title, 
            color = TextDark, 
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (!post.description.isNullOrBlank()) {
             Text(
                text = post.description, 
                color = TextGray, 
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
             )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { 
                isLiked = !isLiked 
                // In full implementation, save to 'likes' table
            }) {
                Icon(
                    if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                    contentDescription = "Like", 
                    tint = if (isLiked) Color.Red else TextDark,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isLiked) "1" else "0", fontWeight = FontWeight.Bold, color = TextDark)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ChatBubbleOutline, 
                    contentDescription = "Comment", 
                    tint = TextDark,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("0", fontWeight = FontWeight.Bold, color = TextDark)
            }
            
            Icon(
                Icons.Default.Send, 
                contentDescription = "Share", 
                tint = TextDark,
                modifier = Modifier.size(26.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                Icons.Default.BookmarkBorder, 
                contentDescription = "Save", 
                tint = TextDark,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }
    
    val coroutineScope = rememberCoroutineScope()

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedMediaUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Post", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
        }

        Divider(color = Color.LightGray.copy(alpha=0.5f))

        Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
            // Media Preview
            if (selectedMediaUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { context ->
                            android.widget.VideoView(context).apply {
                                setVideoURI(selectedMediaUri)
                                start()
                                setOnCompletionListener { start() }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryGreen.copy(alpha = 0.05f))
                        .border(1.dp, PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { mediaPickerLauncher.launch("*/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, contentDescription = "Add Media", tint = PrimaryGreen, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Add Photo or Video", color = PrimaryGreen, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Title Field
            Text("Title", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter post title...") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Description Field
            Text("Description (Optional)", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Say something about this...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isUploading) {
                LinearProgressIndicator(progress = uploadProgress, modifier = Modifier.fillMaxWidth().height(8.dp), color = PrimaryGreen)
                Spacer(modifier = Modifier.height(12.dp))
                if (processing) {
                    Text("Processing... After processing, your video will be available.", color = PrimaryGreen, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
                } else {
                    Text("Uploading to server... ${(uploadProgress * 100).toInt()}%", color = TextGray, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                Spacer(modifier = Modifier.height(32.dp))
            } else {
                val context = androidx.compose.ui.platform.LocalContext.current
                Button(
                    onClick = { 
                        if (titleInput.isNotBlank() && selectedMediaUri != null) {
                             isUploading = true
                             coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                 try {
                                     val client = okhttp3.OkHttpClient.Builder()
                                         .connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                                         .writeTimeout(600, java.util.concurrent.TimeUnit.SECONDS)
                                         .readTimeout(600, java.util.concurrent.TimeUnit.SECONDS)
                                         .build()
                                     
                                     var totalSize = 0L
                                     context.contentResolver.query(selectedMediaUri!!, null, null, null, null)?.use { cursor ->
                                         if (cursor.moveToFirst()) {
                                             val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                                             if (sizeIndex != -1) {
                                                 totalSize = cursor.getLong(sizeIndex)
                                             }
                                         }
                                     }
                                     if (totalSize == 0L) {
                                         // fallback
                                         context.contentResolver.openInputStream(selectedMediaUri!!)?.use { 
                                             totalSize = it.available().toLong() 
                                         }
                                     }
                                     
                                     val mimeTypeStr = context.contentResolver.getType(selectedMediaUri!!) ?: "video/mp4"
                                     val ext = if (mimeTypeStr.startsWith("image")) "jpg" else "mp4"
                                     
                                     val mediaBody = object : okhttp3.RequestBody() {
                                         override fun contentType(): okhttp3.MediaType? = mimeTypeStr.toMediaTypeOrNull()
                                         override fun contentLength(): Long = totalSize
                                         override fun writeTo(sink: okio.BufferedSink) {
                                             context.contentResolver.openInputStream(selectedMediaUri!!)?.use { input ->
                                                 val buffer = ByteArray(8192)
                                                 var read: Int
                                                 var uploaded = 0L
                                                 while (input.read(buffer).also { read = it } != -1) {
                                                     sink.write(buffer, 0, read)
                                                     uploaded += read
                                                     uploadProgress = (uploaded.toFloat() / totalSize.coerceAtLeast(1L).toFloat())
                                                 }
                                             }
                                         }
                                     }
                                     
                                     processing = true
                                     
                                     val bodyBuilder = okhttp3.MultipartBody.Builder().setType(okhttp3.MultipartBody.FORM)
                                         .addFormDataPart("media", "upload.$ext", mediaBody)
                                         
                                     val request = okhttp3.Request.Builder()
                                         .url(com.example.network.ApiConfig.BASE_URL + "api/upload")
                                         .post(bodyBuilder.build())
                                         .build()
                                         
                                     val response = client.newCall(request).execute()
                                     val responseString = response.body?.string()
                                     if (response.isSuccessful) {
                                         val json = org.json.JSONObject(responseString ?: "{}")
                                         val urlStr = json.optJSONObject("media")?.optString("url", "") ?: ""
                                         
                                         val finalUrl = if (urlStr.isNotEmpty()) {
                                             if (urlStr.startsWith("http")) urlStr else "https://$urlStr"
                                         } else {
                                             selectedMediaUri.toString()
                                         }
                                         
                                         val user = com.example.Supabase.client.auth.currentUserOrNull()
                                         val currentUserId = user?.id ?: "anonymous_user"
                                         
                                         val newPost = Post(
                                             userId = currentUserId,
                                             mediaType = if (ext == "mp4") "video" else "photo",
                                             mediaUrl = finalUrl,
                                             title = titleInput,
                                             description = descriptionInput,
                                             userName = user?.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: "User"
                                         )
                                         
                                         try {
                                             com.example.Supabase.client.postgrest["posts"].insert(newPost)
                                         } catch(e: Exception) {
                                             e.printStackTrace()
                                         }
                                         
                                         com.example.social.GlobalPostState.addPost(newPost)
                                         
                                         kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                             onNavigateBack()
                                         }
                                     } else {
                                         val errStr = responseString ?: "Unknown Error"
                                         val errCode = response.code
                                         kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                             android.widget.Toast.makeText(context, "Upload failed! Code $errCode: $errStr", android.widget.Toast.LENGTH_LONG).show()
                                             isUploading = false
                                         }
                                     }
                                 } catch(e: Exception) {
                                     e.printStackTrace()
                                     kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                         android.widget.Toast.makeText(context, "Network Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                         isUploading = false
                                     }
                                 }
                             }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    enabled = !isUploading && titleInput.isNotBlank() && selectedMediaUri != null
                ) {
                    Text("Publish Post", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
