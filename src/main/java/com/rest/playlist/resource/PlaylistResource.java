package com.rest.playlist.resource;

import com.rest.playlist.model.Playlist;
import com.rest.playlist.service.IPlaylistService;
import com.rest.playlist.service.PlaylistServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistResource {

    final private IPlaylistService IPlaylistService;
    private static final Logger log = LoggerFactory.getLogger(PlaylistServiceImpl.class);

    public PlaylistResource(IPlaylistService IPlaylistService) {
        this.IPlaylistService = IPlaylistService;
    }

    @GetMapping
    public ResponseEntity<List<Playlist>> getAllPlaylists() {

        List<Playlist> playlists = IPlaylistService.getAllPlaylists();

        if (playlists.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(playlists, HttpStatus.OK);
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<List<Playlist>> getPlaylistsByTitle(@PathVariable String title) {
        List<Playlist> playlists = IPlaylistService.getPlaylistsByTitle(title);
        if (playlists.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(playlists, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Playlist> getPlaylistById(@PathVariable Long id) {
        Playlist playlist = IPlaylistService.getPlaylistById(id);
        return new ResponseEntity<>(playlist, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Playlist> createPlaylist(@Valid @RequestBody Playlist playlist) {
        Playlist addedPlaylist = IPlaylistService.createPlaylist(playlist);
        return new ResponseEntity<>(addedPlaylist, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity updatePlaylist(@Valid @RequestBody Playlist playlist) {
        Playlist updatedPlaylist = IPlaylistService.updatePlaylist(playlist);
        return new ResponseEntity<>(updatedPlaylist, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deletePlaylistById(@PathVariable Long id) {
        IPlaylistService.deletePlaylistById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
