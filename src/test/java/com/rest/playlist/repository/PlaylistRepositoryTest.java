package com.rest.playlist.repository;

import com.rest.playlist.model.Playlist;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PlaylistRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(PlaylistRepositoryTest.class);

    @Autowired
    PlaylistRepository playlistRepository;
    private Playlist savedPlaylist;

    @Before
    public void setupCreatePlaylist() {
        Playlist playlist = new Playlist();
        playlist.setTitle("Playlist #1");
        playlist.setDescription("Description Playlist #1");

        savedPlaylist = playlistRepository.save(playlist);
        assertThat(savedPlaylist).isNotNull();
        assertThat(savedPlaylist).hasFieldOrPropertyWithValue("title", "Playlist #1");
        assertThat(savedPlaylist).hasFieldOrPropertyWithValue("description", "Description Playlist #1");
    }

    @Test
    public void shouldFindAllPlaylists() {
        List<Playlist> playlists = playlistRepository.findAll();
        assertThat(playlists).isNotEmpty();
        assertThat(playlists).hasSizeGreaterThanOrEqualTo(1);
        assertThat(playlists).contains(playlists.get(playlists.size() - 1));
        assertThat(playlists.get(playlists.size() - 1).getId()).isNotNull();
    }


    @Test
    public void shouldFindPlaylistsByTitle() {
        List<Playlist> playlists = playlistRepository.findPlaylistsByTitleContaining(savedPlaylist.getTitle());
        assertThat(playlists).isNotEmpty();
        assertThat(playlists).hasSizeGreaterThanOrEqualTo(1);
        assertThat(playlists).contains(savedPlaylist);
    }

    @Test
    public void shouldFindPlaylistById() {
        Playlist foundPlaylist = playlistRepository.findById(savedPlaylist.getId()).orElse(null);
        assertThat(foundPlaylist).isNotNull();
        assertThat(foundPlaylist).isEqualTo(savedPlaylist);
        assertThat(foundPlaylist).hasFieldOrPropertyWithValue("title", savedPlaylist.getTitle());
        assertThat(foundPlaylist).hasFieldOrPropertyWithValue("description", savedPlaylist.getDescription());
    }

    @Test
    public void shouldCreatePlaylist() {

        int sizeBeforeCreate = playlistRepository.findAll().size();

        Playlist playlistToSave = new Playlist();
        playlistToSave.setTitle("Playlist #Created");
        playlistToSave.setDescription("Description Playlist #Created");

        Playlist playlist = playlistRepository.save(playlistToSave);

        int sizeAfterCreate = playlistRepository.findAll().size();
        assertThat(sizeAfterCreate).isEqualTo(sizeBeforeCreate + 1);
        assertThat(playlist).isNotNull();
        assertThat(playlist).hasFieldOrPropertyWithValue("title", "Playlist #Created");
        assertThat(playlist).hasFieldOrPropertyWithValue("description", "Description Playlist #Created");

    }

    @Test
    public void shouldUpdatePlaylist() {

        Playlist foundPlaylist = playlistRepository.getById(savedPlaylist.getId());
        assertThat(foundPlaylist).isNotNull();

        foundPlaylist.setTitle("Playlist #Updated");
        foundPlaylist.setDescription("Description Playlist #Updated");

        Playlist updatedPlaylist = playlistRepository.save(foundPlaylist);

        Playlist checkPlaylist = playlistRepository.getById(updatedPlaylist.getId());

        assertThat(checkPlaylist.getId()).isNotNull();
        assertThat(checkPlaylist.getId()).isEqualTo(updatedPlaylist.getId());
        assertThat(checkPlaylist.getTitle()).isEqualTo(updatedPlaylist.getTitle());
        assertThat(checkPlaylist.getDescription()).isEqualTo(updatedPlaylist.getDescription());
    }

    @Test
    public void shouldDeleteSonById() {
        int sizeBeforeDelete = playlistRepository.findAll().size();

        Playlist foundPlaylist = playlistRepository.getById(savedPlaylist.getId());
        assertThat(foundPlaylist).isNotNull();

        playlistRepository.deleteById(foundPlaylist.getId());

        int sizeAfterDelete = playlistRepository.findAll().size();

        assertThat(sizeAfterDelete).isEqualTo(sizeBeforeDelete - 1);
    }

}
