package com.rest.playlist.resource.playlist;

import com.rest.playlist.enums.SongCategory;
import com.rest.playlist.web.exception.ResourceNotFoundException;
import com.rest.playlist.model.Playlist;
import com.rest.playlist.model.Song;
import com.rest.playlist.web.resource.PlaylistResource;
import com.rest.playlist.service.IPlaylistService;
import com.rest.playlist.service.ISongService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.rest.playlist.TestUtils.asJsonString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PlaylistResource.class)
public class PlaylistResourceUnitTest {

    private static final Logger log = LoggerFactory.getLogger(PlaylistResourceUnitTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IPlaylistService playlistService;

    @MockBean
    private ISongService songService;

    private Playlist myPlaylist;
    private List<Playlist> playlistList = new ArrayList<>();


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

        myPlaylist = new Playlist();
        myPlaylist.setTitle("Playlist1");
        myPlaylist.setDescription("Description Playlist1");
        myPlaylist.setSongs(Arrays.asList(savedSong1, savedSong2));
    }

    @Test
    public void testGetAllPlaylists() throws Exception {
        playlistList.add(myPlaylist);
        when(playlistService.getAllPlaylists()).thenReturn(playlistList);

        mockMvc.perform(get("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].title").value(playlistList.get(0).getTitle()))
                .andExpect(jsonPath("$[*].description").value(playlistList.get(0).getDescription()));
        verify(playlistService).getAllPlaylists();
        verify(playlistService, times(1)).getAllPlaylists();
    }

    @Test
    public void testGetEmptyListPlaylists() throws Exception {
        when(playlistService.getAllPlaylists()).thenReturn(playlistList);

        mockMvc.perform(get("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetPlaylistsByTitle() throws Exception {
        playlistList.add(myPlaylist);
        when(playlistService.getPlaylistsByTitle(myPlaylist.getTitle())).thenReturn(playlistList);

        mockMvc.perform(get("/api/playlists/title/{title}", myPlaylist.getTitle())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].title").value(playlistList.get(0).getTitle()))
                .andExpect(jsonPath("$[*].description").value(playlistList.get(0).getDescription()));
    }

    @Test
    public void testGetEmptyListPlaylistsByTitle() throws Exception {
        when(playlistService.getPlaylistsByTitle("No Title")).thenReturn(playlistList);

        mockMvc.perform(get("/api/playlists/title/No Title")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

   @Test
    public void testGetPlaylistById() throws Exception {
        myPlaylist.setId(1000L);
        when(playlistService.getPlaylistById(myPlaylist.getId())).thenReturn(myPlaylist);

        mockMvc.perform(get("/api/playlists/" + myPlaylist.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(myPlaylist.getTitle()))
                .andExpect(jsonPath("$.description").value(myPlaylist.getDescription()));
    }


    @Test
    public void testGetPlaylistByNonExistingId() throws Exception {
        doThrow(new ResourceNotFoundException("Not found Playlist with id = 1000")).when(playlistService).getPlaylistById(1000L);
        mockMvc.perform(get("/api/playlists/1000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found Playlist with id = 1000"));
    }


    @Test
    public void testCreatePlaylist() throws Exception {
        when(playlistService.createPlaylist(any(Playlist.class))).thenReturn(myPlaylist);
        mockMvc.perform(post("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(myPlaylist)))
                .andExpect(status().isCreated());
        verify(playlistService, times(1)).createPlaylist(any());
    }

    @Test
    public void testCreatePlaylistWithTitleSizeLessThanThree() throws Exception {
        myPlaylist.setTitle("S");
        doThrow(new ResourceNotFoundException("Size: titre doit être compris entre 3 et 50 caractères"))
                .when(playlistService).createPlaylist(myPlaylist);
        mockMvc.perform(post("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(myPlaylist)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: titre doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testCreatePlaylistWithDescriptionSizeLessThanThree() throws Exception {
        myPlaylist.setDescription("S");
        doThrow(new ResourceNotFoundException("Size: description doit être compris entre 3 et 50 caractères"))
                .when(playlistService).createPlaylist(myPlaylist);
        mockMvc.perform(post("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(myPlaylist)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: description doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testCreatePlaylistWithTitleNull() throws Exception {
        myPlaylist.setTitle(null);
        doThrow(new ResourceNotFoundException("NotBlank: titre ne doit pas être null ou vide"))
                .when(playlistService).createPlaylist(myPlaylist);
        mockMvc.perform(post("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(myPlaylist)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("NotBlank: titre ne doit pas être null ou vide"));
    }


    @Test
    public void testUpdatePlaylist() throws Exception {
        myPlaylist.setId(1000L);
        when(playlistService.updatePlaylist(myPlaylist)).thenReturn(myPlaylist);
        mockMvc.perform(put("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(myPlaylist)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdatePlaylistWithTitleSizeLessThanThree() throws Exception {
        myPlaylist.setId(1000L);
        myPlaylist.setTitle("S");
        doThrow(new ResourceNotFoundException("Size: titre doit être compris entre 3 et 50 caractères"))
                .when(playlistService).updatePlaylist(myPlaylist);
        mockMvc.perform(post("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(myPlaylist)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: titre doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testUpdatePlaylistWithDescriptionSizeLessThanThree() throws Exception {
        myPlaylist.setId(1000L);
        myPlaylist.setDescription("S");
        doThrow(new ResourceNotFoundException("Size: description doit être compris entre 3 et 50 caractères"))
                .when(playlistService).updatePlaylist(myPlaylist);
        mockMvc.perform(post("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(myPlaylist)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: description doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testUpdatePlaylistWithTitleNull() throws Exception {
        myPlaylist.setId(1000L);
        myPlaylist.setTitle(null);
        doThrow(new ResourceNotFoundException("NotBlank: titre ne doit pas être null ou vide"))
                .when(playlistService).updatePlaylist(myPlaylist);
        mockMvc.perform(post("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(myPlaylist)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("NotBlank: titre ne doit pas être null ou vide"));
    }

    @Test
    public void testDeletePlaylistById() throws Exception {
        myPlaylist.setId(1000L);
        doNothing().when(playlistService).deletePlaylistById(myPlaylist.getId());
        mockMvc.perform(delete("/api/playlists/" + myPlaylist.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNotFoundPlaylist() throws Exception {
        doThrow(new ResourceNotFoundException("Not found Playlist with id = 5000")).when(playlistService).deletePlaylistById(5000L);
        mockMvc.perform(delete("/api/playlists/5000"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found Playlist with id = 5000"));
    }


}
