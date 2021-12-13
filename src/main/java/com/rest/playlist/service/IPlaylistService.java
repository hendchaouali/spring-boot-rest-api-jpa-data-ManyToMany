package com.rest.playlist.service;

import com.rest.playlist.model.Playlist;

import java.util.List;

public interface IPlaylistService {

    List<Playlist> getAllPlaylists();

    Playlist getPlaylistById(Long id);

    List<Playlist> getPlaylistsByTitle(String title);

    Playlist createPlaylist(Playlist playlist);

    Playlist updatePlaylist(Playlist playlist);

    void deletePlaylistById(Long id);
}
