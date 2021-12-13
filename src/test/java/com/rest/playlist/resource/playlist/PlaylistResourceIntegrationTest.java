package com.rest.playlist.resource.playlist;

import com.rest.playlist.enums.SongCategory;
import com.rest.playlist.exception.ServiceExceptionHandler;
import com.rest.playlist.model.Playlist;
import com.rest.playlist.model.Song;
import com.rest.playlist.repository.PlaylistRepository;
import com.rest.playlist.resource.PlaylistResource;
import com.rest.playlist.service.IPlaylistService;
import com.rest.playlist.service.ISongService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static com.rest.playlist.TestUtils.asJsonString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PlaylistResourceIntegrationTest {
    private final static Logger log = LoggerFactory.getLogger(PlaylistResourceIntegrationTest.class);

    private MockMvc mockMvc;

    @Autowired
    private ServiceExceptionHandler serviceExceptionHandler;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private IPlaylistService playlistService;

    @Autowired
    private ISongService songService;

    private Playlist myPlaylist;

    @Before
    public void setup() {

        PlaylistResource playlistResource = new PlaylistResource(playlistService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(playlistResource)
                .setControllerAdvice(serviceExceptionHandler)
                .build();

        Song song1 = new Song();
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
        Playlist savedPlaylist = playlistRepository.saveAndFlush(myPlaylist);
        mockMvc.perform(get("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].title").value(hasItem(savedPlaylist.getTitle())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(savedPlaylist.getDescription())));
    }

    @Test
    public void testGetNoContentPlaylists() throws Exception {
        playlistRepository.deleteAll();
        mockMvc.perform(get("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }


    @Test
    public void testGetPlaylistsByTitle() throws Exception {
        Playlist savedPlaylist = playlistRepository.saveAndFlush(myPlaylist);
        mockMvc.perform(get("/api/playlists/title/{title}", savedPlaylist.getTitle())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].title").value(hasItem(savedPlaylist.getTitle())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(savedPlaylist.getDescription())));
    }

    @Test
    public void testGetPlaylistById() throws Exception {

        Playlist savedPlaylist = playlistRepository.saveAndFlush(myPlaylist);

        mockMvc.perform(get("/api/playlists/{id}", savedPlaylist.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPlaylist.getId()))
                .andExpect(jsonPath("$.title").value(savedPlaylist.getTitle()))
                .andExpect(jsonPath("$.description").value(savedPlaylist.getDescription()));
    }


    @Test
    public void testGetPlaylistByNonExistingId() throws Exception {
        mockMvc.perform(get("/api/playlists/4000"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testCreatePlaylist() throws Exception {
        int sizeBefore = playlistRepository.findAll().size();
        Playlist savedPlaylist = playlistRepository.saveAndFlush(myPlaylist);
        mockMvc.perform(post("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedPlaylist)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(savedPlaylist.getTitle()))
                .andExpect(jsonPath("$.description").value(savedPlaylist.getDescription()));

        List<Playlist> playlists = playlistRepository.findAll();

        assertThat(playlists).hasSize(sizeBefore + 1);

        Playlist lastPlaylist = playlists.get(playlists.size() - 1);

        assertThat(lastPlaylist.getTitle()).isEqualTo(savedPlaylist.getTitle());
        assertThat(lastPlaylist.getDescription()).isEqualTo(savedPlaylist.getDescription());
    }


    @Test
    public void testCreatePlaylistWithTitleSizeLessThanThree() throws Exception {
        myPlaylist.setTitle("S");
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
        Playlist savedPlaylist = playlistRepository.saveAndFlush(myPlaylist);
        savedPlaylist.setTitle("Playlist updated");
        mockMvc.perform(put("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedPlaylist)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdatePlaylistWithTitleSizeLessThanThree() throws Exception {
        Playlist savedPlaylist = playlistRepository.saveAndFlush(myPlaylist);
        savedPlaylist.setTitle("S");
        mockMvc.perform(post("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedPlaylist)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: titre doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testUpdatePlaylistWithDescriptionSizeLessThanThree() throws Exception {
        Playlist savedPlaylist = playlistRepository.saveAndFlush(myPlaylist);
        savedPlaylist.setDescription("S");
        mockMvc.perform(post("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedPlaylist)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: description doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testUpdatePlaylistWithTitleNull() throws Exception {
        Playlist savedPlaylist = playlistRepository.saveAndFlush(myPlaylist);
        savedPlaylist.setTitle(null);
        mockMvc.perform(post("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedPlaylist)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("NotBlank: titre ne doit pas être null ou vide"));
    }


    @Test
    public void testDeletePlaylistById() throws Exception {
        Playlist savedPlaylist = playlistRepository.saveAndFlush(myPlaylist);
        mockMvc.perform(delete("/api/playlists/{id}", savedPlaylist.getId()))
                .andExpect(status().isNoContent());

    }

    @Test
    public void testDeleteNotFoundPlaylist() throws Exception {
        mockMvc.perform(delete("/api/playlists/1000"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found playlist with id = 1000"));
    }
}
