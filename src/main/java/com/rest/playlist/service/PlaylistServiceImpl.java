package com.rest.playlist.service;

import com.rest.playlist.web.exception.ResourceNotFoundException;
import com.rest.playlist.model.Playlist;
import com.rest.playlist.model.Song;
import com.rest.playlist.repository.PlaylistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlaylistServiceImpl implements IPlaylistService {
    private static final Logger log = LoggerFactory.getLogger(PlaylistServiceImpl.class);

    private final PlaylistRepository playlistRepository;
    private final ISongService songService;

    public PlaylistServiceImpl(PlaylistRepository playlistRepository, ISongService songService) {
        this.playlistRepository = playlistRepository;
        this.songService = songService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Playlist> getPlaylistsByTitle(String title) {
        return playlistRepository.findPlaylistsByTitleContaining(title);
    }


    @Override
    @Transactional(readOnly = true)
    public Playlist getPlaylistById(Long id) {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found playlist with id = " + id));
    }

    @Override
    public Playlist createPlaylist(Playlist playlist) {
        if(!playlist.getSongs().isEmpty()){
            List<Song> songs = playlist.getSongs()
                    .stream()
                    .map(song -> songService.getSongById(song.getId()))
                    .collect(Collectors.toList());
            playlist.setSongs(songs);
        }
        return playlistRepository.save(playlist);
    }

    @Override
    public Playlist updatePlaylist(Playlist playlist) {

        Playlist searchedPlaylist = playlistRepository.findById(playlist.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Not found playlist with id = " + playlist.getId()));

        searchedPlaylist.setTitle(playlist.getTitle());
        searchedPlaylist.setDescription(playlist.getDescription());
        if(!playlist.getSongs().isEmpty()){
            List<Song> songs = playlist.getSongs()
                    .stream()
                    .map(song -> songService.getSongById(song.getId()))
                    .collect(Collectors.toList());
            searchedPlaylist.setSongs(songs);
        }
        return playlistRepository.saveAndFlush(playlist);
    }

    @Override
    public void deletePlaylistById(Long id) {
        playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found playlist with id = " + id));

        playlistRepository.deleteById(id);
    }
}
