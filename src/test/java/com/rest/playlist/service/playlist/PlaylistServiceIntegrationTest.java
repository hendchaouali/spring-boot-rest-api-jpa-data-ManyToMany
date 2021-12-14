package com.rest.playlist.service.playlist;

import com.rest.playlist.enums.SongCategory;
import com.rest.playlist.web.exception.ResourceNotFoundException;
import com.rest.playlist.model.Playlist;
import com.rest.playlist.model.Song;
import com.rest.playlist.repository.PlaylistRepository;
import com.rest.playlist.service.PlaylistServiceImpl;
import com.rest.playlist.service.SongServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

@SpringBootTest
@Transactional
@RunWith(SpringRunner.class)
public class PlaylistServiceIntegrationTest {

    private final static Logger log = LoggerFactory.getLogger(PlaylistServiceIntegrationTest.class);

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistServiceImpl playlistService;

    @Autowired
    private SongServiceImpl songService;

    private Playlist defaultPlaylist;

    @Before
    public void setup() {

        Song song1 =  new Song();
        song1.setTitle("For The Lover That I Lost");
        song1.setDescription("Live At Abbey Road Studios");
        song1.setCategory(SongCategory.POP);
        song1.setArtistName("Sam Smith");
        song1.setDuration("3:01");

        Song song2 = new Song();
        song2.setTitle("Summer Rain");
        song2.setDescription("Live At Road Studios");
        song2.setCategory(SongCategory.POP);
        song2.setArtistName("Lean Bridges");
        song2.setDuration("4:01");

        Song savedSong1 = songService.createSong(song1);
        Song savedSong2 = songService.createSong(song2);

        Playlist myPlaylist = new Playlist();
        myPlaylist.setTitle("Playlist1");
        myPlaylist.setDescription("Description Playlist1");
        myPlaylist.setSongs(Arrays.asList(savedSong1, savedSong2));

        defaultPlaylist = playlistRepository.saveAndFlush(myPlaylist);

    }

    @Test
    public void testGetAllPlaylists() {
        List<Playlist> playlists = playlistService.getAllPlaylists();
        assertThat(playlists).isNotNull().isNotEmpty();
    }

    @Test
    public void testGetPlaylistsByArtistName() {
        List<Playlist> playlists = playlistService.getPlaylistsByTitle("Playlist");
        assertThat(playlists).isNotNull().isNotEmpty();
    }

    @Test
    public void testGetPlaylistById() {
        Playlist playlist = playlistService.getPlaylistById(defaultPlaylist.getId());
        assertThat(playlist).isNotNull();
        assertThat(playlist.getId()).isNotNull();
        assertThat(playlist.getId()).isEqualTo(defaultPlaylist.getId());
        assertThat(playlist.getTitle()).isEqualTo(defaultPlaylist.getTitle());
        assertThat(playlist.getDescription()).isEqualTo(defaultPlaylist.getDescription());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetPlaylistWithNonExistingId() {
        playlistService.getPlaylistById(4000L);
    }

    @Test
    public void testCreatePlaylist() {
        Playlist savedPlaylist = playlistService.createPlaylist(defaultPlaylist);
        assertThat(savedPlaylist).isNotNull();
        assertThat(savedPlaylist.getId()).isNotNull();
        assertThat(savedPlaylist.getTitle()).isEqualTo(defaultPlaylist.getTitle());
        assertThat(savedPlaylist.getDescription()).isEqualTo(defaultPlaylist.getDescription());
    }

    @Test
    public void testUpdatePlaylist() {
        defaultPlaylist.setTitle("Updated Playlist");
        defaultPlaylist.setDescription("Updated Description Playlist");

        Playlist updatedPlaylist = playlistService.updatePlaylist(defaultPlaylist);
        assertThat(updatedPlaylist).isNotNull();
        assertThat(updatedPlaylist.getId()).isNotNull();
        assertThat(updatedPlaylist.getTitle()).isEqualTo("Updated Playlist");
        assertThat(updatedPlaylist.getDescription()).isEqualTo("Updated Description Playlist");

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testUpdatePlaylistWithNonExistingId() {
        defaultPlaylist.setId(4000L);
        playlistService.updatePlaylist(defaultPlaylist);

    }

    @Test
    public void testDeletePlaylistById() {
        playlistService.deletePlaylistById(defaultPlaylist.getId());
        Optional<Playlist> deletedPlaylist = playlistRepository.findById(defaultPlaylist.getId());
        assertThat(deletedPlaylist.isPresent()).isFalse();

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testDeletePlaylistWithNonExistingId() {
        playlistService.deletePlaylistById(4000L);

    }

}
