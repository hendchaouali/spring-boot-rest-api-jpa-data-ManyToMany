package com.rest.playlist.service.playlist;

import com.rest.playlist.enums.SongCategory;
import com.rest.playlist.exception.ResourceNotFoundException;
import com.rest.playlist.model.Playlist;
import com.rest.playlist.model.Song;
import com.rest.playlist.repository.PlaylistRepository;
import com.rest.playlist.repository.SongRepository;
import com.rest.playlist.service.ISongService;
import com.rest.playlist.service.PlaylistServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class PlaylistServiceUnitTest {

    private final static Logger log = LoggerFactory.getLogger(PlaylistServiceUnitTest.class);

    @MockBean
    private PlaylistRepository playlistRepository;

    @MockBean
    private ISongService songService;

    private PlaylistServiceImpl playlistService;

    private Playlist myPlaylist;
    private List<Playlist> playlistList = new ArrayList<>();
    private List<Song> songsList = new ArrayList<>();


    @Before
    public void setup() {
        playlistService = new PlaylistServiceImpl(playlistRepository, songService);

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


        songsList.addAll(Arrays.asList(song1, song2));

        myPlaylist = new Playlist();
        myPlaylist.setId(1000L);
        myPlaylist.setTitle("Playlist1");
        myPlaylist.setDescription("Description Playlist1");
        myPlaylist.setSongs(songsList);
    }

    @Test
    public void testGetAllPlaylists() {
        playlistRepository.save(myPlaylist);
        when(playlistRepository.findAll()).thenReturn(playlistList);

        //test
        List<Playlist> playlists = playlistService.getAllPlaylists();
        assertEquals(playlists, playlistList);
        verify(playlistRepository, times(1)).save(myPlaylist);
        verify(playlistRepository, times(1)).findAll();
    }

    @Test
    public void testGetPlaylistsByTitle() {
        playlistList.add(myPlaylist);
        when(playlistRepository.findPlaylistsByTitleContaining(myPlaylist.getTitle())).thenReturn(playlistList);
        List<Playlist> playlists = playlistService.getPlaylistsByTitle(myPlaylist.getTitle());

        //test
        assertThat(playlists).isNotEmpty();
        assertThat(playlists).hasSizeGreaterThanOrEqualTo(1);
        verify(playlistRepository, times(1)).findPlaylistsByTitleContaining(myPlaylist.getTitle());
    }

    @Test
    public void testCreatePlaylist() {
        when(songService.createSong(songsList.get(0))).thenReturn(songsList.get(0));
        when(songService.createSong(songsList.get(1))).thenReturn(songsList.get(0));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(myPlaylist);
        playlistService.createPlaylist(myPlaylist);
        verify(playlistRepository, times(1)).save(any(Playlist.class));
    }

    @Test
    public void testUpdatePlaylist() {
        when(songService.createSong(songsList.get(0))).thenReturn(songsList.get(0));
        when(songService.createSong(songsList.get(1))).thenReturn(songsList.get(0));
        when(playlistRepository.findById(myPlaylist.getId())).thenReturn(Optional.of(myPlaylist));

        myPlaylist.setTitle("Playlist #1");
        myPlaylist.setDescription("Description Playlist #1");

        given(playlistRepository.saveAndFlush(myPlaylist)).willReturn(myPlaylist);

        Playlist updatedPlaylist = playlistService.updatePlaylist(myPlaylist);

        assertThat(updatedPlaylist).isNotNull();
        assertThat(updatedPlaylist).isEqualTo(myPlaylist);
        assertThat(updatedPlaylist.getId()).isNotNull();
        assertThat(updatedPlaylist.getId()).isEqualTo(myPlaylist.getId());
        assertThat(updatedPlaylist.getTitle()).isEqualTo(myPlaylist.getTitle());
        assertThat(updatedPlaylist.getDescription()).isEqualTo(myPlaylist.getDescription());
    }


    @Test(expected = ResourceNotFoundException.class)
    public void testUpdatePlaylistWithNonExistingId() {
        when(playlistRepository.findById(myPlaylist.getId())).thenReturn(Optional.empty());
        playlistService.updatePlaylist(myPlaylist);

    }

    @Test
    public void testGetPlaylistsById() {
        // when
        when(playlistRepository.findById(myPlaylist.getId())).thenReturn(Optional.ofNullable(myPlaylist));
        Playlist foundPlaylist = playlistService.getPlaylistById(myPlaylist.getId());

        //test - then
        assertThat(foundPlaylist).isNotNull();
        assertThat(foundPlaylist).isEqualTo(myPlaylist);
        assertThat(foundPlaylist.getId()).isNotNull();
        assertThat(foundPlaylist.getId()).isEqualTo(1000L);
        assertThat(foundPlaylist.getId()).isEqualTo(myPlaylist.getId());
        assertThat(foundPlaylist.getTitle()).isEqualTo(myPlaylist.getTitle());
        assertThat(foundPlaylist.getDescription()).isEqualTo(myPlaylist.getDescription());

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetPlaylistsWithNonExistingId() {

        // when
        when(playlistRepository.findById(4000L)).thenReturn(Optional.empty());
        playlistService.getPlaylistById(4000L);
    }

    @Test
    public void testGetPlaylistsWithNonExistingIdV2() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> playlistService.getPlaylistById(4000L));

        assertThat(ex.getMessage()).isEqualTo("Not found playlist with id = 4000");
    }

    @Test
    public void testDeletePlaylistById() {
        when(playlistRepository.findById(myPlaylist.getId())).thenReturn(Optional.of(myPlaylist));
        playlistService.deletePlaylistById(myPlaylist.getId());
        verify(playlistRepository, times(1)).deleteById(myPlaylist.getId());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testDeletePlaylistWithNonExistingId() {
        when(playlistRepository.findById(4000L)).thenReturn(Optional.empty());
        playlistService.deletePlaylistById(4000L);
    }
}
