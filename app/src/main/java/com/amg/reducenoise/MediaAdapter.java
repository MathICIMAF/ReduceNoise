package com.amg.reducenoise;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/* loaded from: classes.dex */
public class MediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<MediaFile> files;

    public MediaAdapter(List<MediaFile> files, Context context) {
        this.files = files;
        this.context = context;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AudioViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_item, parent, false));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AudioViewHolder mediaViewHolder = (AudioViewHolder) holder;
        final MediaFile mediaFile = this.files.get(position);
        mediaViewHolder.name.setText(mediaFile.getName());
        mediaViewHolder.lastModified.setText(mediaFile.getLastModification());
        mediaViewHolder.size.setText(mediaFile.getSize());
        if (mediaFile.getType() == MediaType.Audio){
            mediaViewHolder.icon.setBackground(context.getDrawable(R.drawable.baseline_audio_file_24));
        }
        else {
            mediaViewHolder.icon.setBackground(context.getDrawable(R.drawable.baseline_video_file_24));
        }
        mediaViewHolder.share.setOnClickListener(new View.OnClickListener() { // from class: com.amg.compressaudio.AudiosAdapter.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Uri fromFile;
                Intent intent = new Intent("android.intent.action.SEND");

                Context applicationContext = MediaAdapter.this.context.getApplicationContext();
                fromFile = FileProvider.getUriForFile(applicationContext, MediaAdapter.this.context.getPackageName() + ".provider", mediaFile.getFile());

                if (mediaFile.getType() == MediaType.Audio) {
                    intent.setType("audio/");
                }
                else {
                    intent.setType("video/");
                }
                intent.putExtra("android.intent.extra.STREAM", fromFile);
                MediaAdapter.this.context.startActivity(Intent.createChooser(intent, "Share file"));
            }
        });
        mediaViewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                files.remove(mediaFile);
                mediaFile.getFile().delete();
                notifyItemRemoved(position);
            }
        });
        mediaViewHolder.layout.setOnClickListener(new View.OnClickListener() { // from class: com.amg.compressaudio.AudiosAdapter.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                try {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");

                    String mimeTypeFromExtension = (mediaFile.getType()==MediaType.Audio)?MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3"):
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp4");
                    Uri uriForFile = FileProvider.getUriForFile(MediaAdapter.this.context, "com.amg.reducenoise.provider", mediaFile.getFile());
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uriForFile, mimeTypeFromExtension);
                    MediaAdapter.this.context.startActivity(intent);
                } catch (Exception e) {
                    Log.e(e.toString(), e.getMessage());
                }
            }
        });
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.files.size();
    }

    /* loaded from: classes.dex */
    public static class AudioViewHolder extends RecyclerView.ViewHolder {
        public TextView lastModified;
        public LinearLayout layout;
        public TextView name;
        public ImageButton share,delete;
        public TextView size;
        public ImageView icon;

        public AudioViewHolder(View itemView) {
            super(itemView);
            this.layout = (LinearLayout) itemView.findViewById(R.id.audio_layout);
            this.name = (TextView) itemView.findViewById(R.id.title);
            this.size = (TextView) itemView.findViewById(R.id.size);
            this.share = (ImageButton) itemView.findViewById(R.id.share);
            this.lastModified = (TextView) itemView.findViewById(R.id.modification);
            this.delete = (ImageButton)itemView.findViewById(R.id.delete);
            this.icon = (ImageView) itemView.findViewById(R.id.icon);
        }
    }
}
