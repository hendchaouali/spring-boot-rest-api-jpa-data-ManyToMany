# Spring Boot Rest Api Data JPA ManyToMany - Playlists
Dans ce tutoriel, nous allons écrire un service CRUD REST Spring Boot basé sur un mappage unidirectionnel pour une relation JPA et Hibernate ManyToMany et entièrement couvert par des tests.

La relation ManyToMany fait référence à la relation entre deux tables :

* **Song** : a un identifiant, un titre, une description, une catégorie, une durée et le nom d'un artiste.

* **Playlist** : a un titre et une description


Une Chanson peut être liée à plusieurs playlists, et une playlist est liée à plusieurs artiste.

Apis aide à créer, récupérer, mettre à jour, supprimer des Song et des playlists.

Apis prend également en charge les méthodes de recherche personnalisées (query methods) telles que la recherche par catégorie ou par le nom de l’artiste ou par titre de playlist.

##### La relation OneToMany
– L’annotation **@ManyToMany** définit une relation n:n entre deux entités.

 L’annotation **@JoinTable** nous permet de définir une table d'association est aussi appelé table de jointure :

Nous fournissons le nom de la table de jointure ainsi que les clés étrangères avec les annotations **@JoinColumn** . 

L' attribut joinColumn se connectera au côté propriétaire de la relation et l' inverseJoinColumn à l'autre côté.

##### Spring Boot
Spring Boot est un projet Spring qui facilite le processus de configuration et de publication des applications.

En suivant des étapes simples, vous pourrez exécuter votre premier projet.

##### API REST (Representational State Transfer Application Program Interface)
Il se base sur le protocole **HTTP** pour transférer des informations. 
Un client lance une requête HTTP, et le serveur renvoie une réponse à travers plusieurs méthodes dont les plus utilisées sont : **POST**, **GET**, **PUT** et **DELETE**.

##### Outils utilisés : 
* Java 8
* IDE Intellij IDEA
* Spring Boot 2.5.7 (avec Spring Web MVC et Spring Data JPA)
* PostgreSQL
* H2 Database
* Lombok 1.18.22
* Maven 4.0.0


## Initialisation du projet
Pour amorcer une application Spring Boot , nous pouvons démarrer le projet à partir de zéro avec notre IDE préféré, ou simplement utiliser un autre moyen qui facilite la vie : [SpringInitializr](https://start.spring.io/)

Initialement, nous avons choisi les dépendances suivantes : Spring web, Spring Data JPA, Validation, H2 Database, Lombok et PostgreSQL Driver.

![Screenshot](src/main/resources/images/springInitializer.PNG)

## Structure du projet
L'image ci-dessous montre la structure finale du projet

![Screenshot](src/main/resources/images/structure-projet.png)

* **Pom.xml**

Contient des dépendances pour Spring Boot. Dans notre cas, nous sommes besoin de ces dépendances.

```xml
	<dependencies>
    		<dependency>
    			<groupId>org.springframework.boot</groupId>
    			<artifactId>spring-boot-starter-data-jpa</artifactId>
    			<version>2.5.7</version>
    		</dependency>
    		<dependency>
    			<groupId>org.springframework.boot</groupId>
    			<artifactId>spring-boot-starter-validation</artifactId>
    		</dependency>
    		<dependency>
    			<groupId>org.springframework.boot</groupId>
    			<artifactId>spring-boot-starter-web</artifactId>
    		</dependency>
    
    		<dependency>
    			<groupId>org.postgresql</groupId>
    			<artifactId>postgresql</artifactId>
    			<scope>runtime</scope>
    		</dependency>
    		<dependency>
    			<groupId>org.springframework.boot</groupId>
    			<artifactId>spring-boot-starter-test</artifactId>
    			<scope>test</scope>
    		</dependency>
            <dependency>
                <groupId>io.springfox</groupId>
                <artifactId>springfox-swagger2</artifactId>
                <version>2.9.2</version>
            </dependency>
    		<dependency>
    			<groupId>io.springfox</groupId>
    			<artifactId>springfox-swagger-ui</artifactId>
    			<version>2.9.2</version>
    		</dependency>
            <dependency>
                <groupId>org.apache.commons</groupId>
                <artifactId>commons-lang3</artifactId>
                <version>3.9</version>
            </dependency>
            <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <scope>test</scope>
            </dependency>
    		<dependency>
    			<groupId>org.hibernate</groupId>
    			<artifactId>hibernate-envers</artifactId>
    			<version>5.6.1.Final</version>
    		</dependency>
    		<dependency>
    			<groupId>org.projectlombok</groupId>
    			<artifactId>lombok</artifactId>
    			<version>1.18.22</version>
    		</dependency>
    		<dependency>
    			<groupId>com.h2database</groupId>
    			<artifactId>h2</artifactId>
    			<scope>test</scope>
    		</dependency>
    	</dependencies>
```

* **Main Class**

C’est la classe principale de l’application et appelée aussi une classe de démarrage.

L ’adresse par défaut d’exécution : http://localhost:8080 

```java 
@SpringBootApplication
public class PlaylistApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlaylistApplication.class, args);
	}

}
```

## I. Configuration PostgreSQL
* **application.properties**

Les propriétés **spring.datasource.username** et **spring.datasource.password** sont les mêmes que celles de votre installation de base de données.

Spring Boot utilise Hibernate pour l'implémentation JPA, nous configurons PostgreSQLDialect pour PostgreSQL 🡺 Ce dialecte nous permet de générer de meilleures requêtes SQL pour cette base de données.

**spring.jpa.hibernate.ddl-auto= update** est utilisé pour créer automatiquement les tables en fonction des classes d’entités dans l’application. Toute modification du modèle déclenche également une mise à jour de la table. 

Pour la production, cette propriété doit être **validate**, cette valeur valide le schéma en correspondance avec le mapping hibernate.


```yaml
spring.datasource.url=jdbc:postgresql://localhost:5432/playlist_song_db
spring.datasource.username=playlistadmin
spring.datasource.password=admin

spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect

spring.jpa.hibernate.ddl-auto=update
```
## II. Modèle
* **AbstractAuditModel**
Les deux modèles de l’application Playlist et Song auront des champs communs liés à l'audit tels que createdAt et updatedAt.

Il est préférable de faire abstraction de ces champs communs dans une classe de base distincte appelée AbstractAuditModel. Cette classe sera étendue par d'autres entités.

**@EntityListeners(AuditingEntityListener.class)** : les valeurs de createdAt et updatedAt seront automatiquement renseignées lorsque les entités seront conservées.

**@MappedSuperclass.java**

En utilisant la stratégie MappedSuperclass, l'héritage n'est évident que dans la classe mais pas dans le modèle d'entité. Il faut noter que cette classe n'a plus d’annotation @Entity, car elle ne sera pas conservée seule dans la base de données.

```java
@MappedSuperclass
@Audited
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditModel implements Serializable {

    @CreatedDate
    @JsonIgnore
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createAt = Instant.now();

    @LastModifiedDate
    @JsonIgnore
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Instant getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Instant createAt) {
        this.createAt = createAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
```
**@EnableJpaAuditing** : Pour activer l'audit JPA (dans la classe de repository)

* **Song.java**

L’entité « Song » est mappé à une table nommée « songs » dans la base de données

- l'annotation **@Entity** indique que la classe est une classe Java persistante.

– l'annotation **@Table** fournit la table qui mappe cette entité.

– l'annotation **@Id** est pour la clé primaire.

– l'annotation **@GeneratedValue** est utilisée pour définir la stratégie de génération de la clé primaire. **GenerationType.SEQUENCE** signifie la génération de la clé primaire se fera par une séquence définie dans le SGBD, auquel on ajoute l’attribut generator.

– l'annotation **@Column** est utilisée pour définir la colonne dans la base de données qui mappe le champ annoté.
  
– l'annotation **@JsonIgnore**  est utilisé au niveau du champ pour marquer une propriété ou une liste de propriétés à ignorer.

– **FetchType.LAZY** indique que la relation doit être chargée à la demande.

– **FetchType.EAGER** indique que la relation doit être chargée en même temps que l'entité qui la porte.

Ici, nous allons utiliser **Lombok** : est une bibliothèque Java qui se connecte automatiquement à un éditeur afin de générer automatiquement les méthodes getter ou equals à l'aide des annotations.
  
* **@Getter / @Setter** :  pour générer automatiquement le getter/setter par défaut.

```java
@Entity
@Getter
@Setter
@Table(name = "songs")
public class Song extends AbstractAuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SONG_SEQ")
    @SequenceGenerator(name = "SONG_SEQ", sequenceName = "song_seq", allocationSize = 1)
    private Long id;

    @Column(name = "title")
    @NotBlank(message = "titre ne doit pas être null ou vide")
    @Size(min = 3, max = 50, message = "titre doit être compris entre 3 et 50 caractères")
    private String title;

    @Column(name = "description")
    @NotBlank(message = "description ne doit pas être nulle ou vide")
    @Size(min = 3, max = 50, message = "description doit être compris entre 3 et 50 caractères")
    private String description;

    @Column(name = "duration")
    @NotBlank(message = "duration ne doit pas être nulle")
    private String duration;

    @Column(name = "artist_name")
    @NotBlank(message = "artistname ne doit pas être null")
    private String artistName;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "categorie<JAZZ, POP, CLASSICAL> ne doit pas être nulle")
    private SongCategory category;


    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Playlist> playlist;
}
```
* **Playlist.java**

L’entité « Playlist » est mappé à une table nommée « playlists » dans la base de données

```java
@Getter
@Setter
@Entity
@Table(name = "playlists")
public class Playlist extends AbstractAuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PLAYLIST_SEQ")
    @SequenceGenerator(name = "PLAYLIST_SEQ", sequenceName = "playlist_seq", allocationSize = 1)
    private Long id;

    @Column(name = "title")
    @NotBlank(message = "titre ne doit pas être null ou vide")
    @Size(min = 3, max = 50, message = "titre doit être compris entre 3 et 50 caractères")
    private String title;

    @Column(name = "description")
    @NotBlank(message = "duration ne doit pas être nulle ou vide")
    @Size(min = 3, max = 50, message = "description doit être compris entre 3 et 50 caractères")
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "song_playlist",
            joinColumns = {@JoinColumn(name = "playlist_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "song_id", referencedColumnName = "id")})
    private List<Song> songs;
}
```

### III. enums
La classe « **SongCategory** » contient les différentes valeurs possibles d’une catégorie.

```java 
public enum SongCategory {
    JAZZ,
    CLASSICAL,
    POP
}
```
## IV. Reposirory
Spring framework nous fournit des repositories afin d’encapsuler des détails de la couche de persistance et de fournir une interface CRUD pour une seule entité ⇒ la possibilité de générer toutes sortes d'opérations vers la base de données.

**Spring Data JPA** est le module qui nous permet d’interagir avec une base de données relationnelles en représentant les objets du domaine métier sous la forme d’entités JPA.

L’annotation **@Repository** est une spécialisation de l’annotation **@Component** ⇒ Pour indiquer que la classe définit un référentiel de données

* **SongRepository.java**

Cette interface est utilisée pour accéder aux chansons de la base de données et qui s'étend de JpaRepository.

Avec **JpaRepository**, nous pouvons :

 * Bénéficier automatiquement des méthodes héritées tels que : **findAll(), findById()** …
 * Utiliser les "query methods" qui utilise une convention de nom pour générer automatiquement le code sous-jacent et exécuter la requête tels que :
   
    – **findSongsByCategory()**: renvoie toutes les chansons ayant une valeur de category en paramètre (JAZZ, POP, CLASSICAL).
   
    – **findSongsByArtistNameContaining()**: renvoie toutes les chansons qui ont le nom de l’artiste en paramètre.

```java
@Repository
@EnableJpaAuditing
public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findSongsByCategory(SongCategory category);
    List<Song> findSongsByArtistNameContaining(String artistName);
}
```
* **PlaylistRepository.java**

```java
@Repository
@EnableJpaAuditing
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findPlaylistsByTitleContaining(String title);
}
```

## V. Service
* **ISongService**

```java
public interface ISongService {

    List<Song> getAllSongs();

    List<Song> getSongsByCategory(String category);

    List<Song> getSongsByArtistName(String artistName);

    Song getSongById(Long id);

    Song createSong(Song song);

    Song updateSong(Song song);

    void deleteSongById(Long id);
}
```

* **IPlaylistService**

```java
public interface IPlaylistService {

    List<Playlist> getAllPlaylists();

    Playlist getPlaylistById(Long id);

    List<Playlist> getPlaylistsByTitle(String title);

    Playlist createPlaylist(Playlist playlist);

    Playlist updatePlaylist(Playlist playlist);

    void deletePlaylistById(Long id);
}
```
* **SongServiceImpl**

L'annotation **@Transactional** peut être utilisée pour indiquer au conteneur les méthodes qui doivent s'exécuter dans un contexte transactionnel.

L’annotation **@Transactional(readOnly = true)** permet d’indiquer si la transaction est en lecture seule (false par défaut) ⇒ Pour les interactions avec les bases de données, les transactions en lecture seule signifient que l’on n’effectue que des requêtes pour lire des données.

```java
@Service
@Transactional
public class SongServiceImpl implements ISongService {
    private static final Logger log = LoggerFactory.getLogger(SongServiceImpl.class);

    private final SongRepository songRepository;

    public SongServiceImpl(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> getSongsByCategory(String category) {
        SongCategory searchedCategory = EnumUtils.getEnumIgnoreCase(SongCategory.class, category);
        if (searchedCategory == null) {
            throw new ResourceNotFoundException("Not found Category with value = " + category);
        }
        return songRepository.findSongsByCategory(searchedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> getSongsByArtistName(String artistName) {
        return songRepository.findSongsByArtistNameContaining(artistName);
    }


    @Override
    @Transactional(readOnly = true)
    public Song getSongById(Long id) {
        return songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found song with id = " + id));
    }

    @Override
    public Song createSong(Song song) {return songRepository.save(song);}

    @Override
    public Song updateSong(Song song) {

        Song searchedSong = songRepository.findById(song.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Not found song with id = " + song.getId()));

        searchedSong.setTitle(song.getTitle());
        searchedSong.setDescription(song.getDescription());
        searchedSong.setArtistName(song.getArtistName());
        searchedSong.setCategory(song.getCategory());
        searchedSong.setDuration(song.getDuration());

        return songRepository.saveAndFlush(song);
    }

    @Override
    public void deleteSongById(Long id) {
        songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found song with id = " + id));

        songRepository.deleteById(id);
    }
}
```

* **PlaylistServiceImpl**

```java
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
```
## VI. Resource
* **SongResource**

Ce contrôleur expose des end-point pour faire les CRUD (créer, récupérer, mettre à jour, supprimer et trouver) des chansons.

##### Points de terminaison d’API

| Méthode HTTP | URI | Description | Codes d'états http valides |
| ------------- | ------------- | ------------- | ------------- |
| POST  | /api/songs  | Créer une chanson  | 201  |
| PUT  | /api/songs/{id}  | Modifier une chanson  | 200  |
| GET  | /api/songs/{id}  | Récupérer une chanson | 200  |
| GET  | /api/songs  | Récupérer toutes les chansons  | 200, 204  |
| GET  | /api/songs/category/{category} | Récupérer toutes les chansons par catégorie  | 200, 204  |
| GET  | /api/songs/artist/{artistName} | Récupérer toutes les chansons par nom d'artiste  | 200, 204  |
| DELETE  | /api/songs/{id}  | Supprimer une chanson | 204  |

– l'annotation **@RestController** est utilisée pour définir un contrôleur.

⇒ **@RestController** remplace principalement :

**@Controller** : pour dire que c'est un controlleur, pour que spring le charge dans son context, et pour le rendre singleton.

**@ResponseBody** : pour indiquer que la valeur de retour des méthodes doit être liée au corps de la réponse Web.

**@RequestMapping("/api/songs")** déclare que toutes les URL d'Apis dans le contrôleur commenceront par /api/songs.

– Nous avons injecté la classe **ISongService** par constructeur.

```java
@RestController
@RequestMapping("/api/songs")
public class SongResource {

    final private ISongService ISongService;
    private static final Logger log = LoggerFactory.getLogger(SongServiceImpl.class);

    public SongResource(ISongService ISongService) {
        this.ISongService = ISongService;
    }

    @GetMapping
    public ResponseEntity<List<Song>> getAllSongs() {

        List<Song> songs = ISongService.getAllSongs();

        if (songs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(songs, HttpStatus.OK);
    }


    @GetMapping("/category/{category}")
    public ResponseEntity<List<Song>> getSongsByCategory(@PathVariable String category) {
        List<Song> songs = ISongService.getSongsByCategory(category);
        if (songs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(songs, HttpStatus.OK);
    }

    @GetMapping("/artist/{artistName}")
    public ResponseEntity<List<Song>> getSongsByArtist(@PathVariable String artistName) {
        List<Song> songs = ISongService.getSongsByArtistName(artistName);
        if (songs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(songs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Song> getSongById(@PathVariable Long id) {
        Song song = ISongService.getSongById(id);
        return new ResponseEntity<>(song, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Song> createSong(@Valid @RequestBody Song song) {
        Song addedSong = ISongService.createSong(song);
        return new ResponseEntity<>(addedSong, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity updateSong(@Valid @RequestBody Song song) {
        return new ResponseEntity<>(ISongService.updateSong(song), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteSongById(@PathVariable Long id) {
        ISongService.deleteSongById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
```
* **PlaylistResource**

Ce contrôleur expose des end-point pour faire les CRUD (créer, récupérer, mettre à jour, supprimer et trouver) des playlists.

##### Points de terminaison d’API

| Méthode HTTP | URI | Description | Codes d'états http valides |
| ------------- | ------------- | ------------- | ------------- |
| POST  | /api/playlists  | Créer une playlist  | 201  |
| PUT  | /api/playlists/{id}  | Modifier une playlist  | 200  |
| GET  | /api/playlists/{id}  | Récupérer une playlist | 200 |
| GET  | /api/playlists/title/{title}  | Récupérer une playlist par titre | 200, 204  |
| GET  | /api/playlists  | Récupérer toutes les playlists  | 200, 204  |
| DELETE  | /api/playlists/{id}  | Supprimer une playlist | 204  |


**@RequestMapping("/api/playlists")** déclare que toutes les URL d'Apis dans le contrôleur commenceront par /api/playlists.

– Nous avons injecté la classe **IPlaylistService** par constructeur.

```java
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
```
## VII. Documentation des API Spring Rest à l'aide de Swagger : Package « config »
Swagger est le framework d'API le plus populaire avec une prise en charge de plus de 40 langues différentes. Nous pouvons utiliser swagger pour concevoir, construire et documenter nos REST API.

```java
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(paths()::test)
                .build();

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Swagger Playlists APIs")
                .description("This page lists all the rest apis for Playlists App.")
                .version("1.0")
                .build();
    }

    private Predicate<String> paths() {
        return ((Predicate<String>) regex("/error.*")::apply).negate()
                .and(regex("/.*")::apply);
    }
}
```

Utiliser cette url : **http://localhost:8080/swagger-ui.html**

![Screenshot](src/main/resources/images/swagger-ui.png)

## VIII. Exceptions

* **@Builder** : nous permet de produire automatiquement le code requis pour que la classe soit instanciable et aussi pour éviter la complexité des constructeurs

* La classe **ErrorMessage**

```java
@Getter
class ErrorMessage {

    private int statusCode;
    private Date timeStamp;
    private String message;
    private String description;
    private List<FieldError> fieldErrors;

    @Builder
    private ErrorMessage(int statusCode, Date timeStamp, String message, String description, List<FieldError> fieldErrors) {
        this.statusCode = statusCode;
        this.timeStamp = timeStamp;
        this.message = message;
        this.description = description;
        this.fieldErrors = fieldErrors;
    }
}
```

* La classe **FieldError**

```java
/**
 * instead of using default error response provided by Spring Boot,
 * FieldError class is part of ErrorMessage class to definr error response message
 */

@Getter
class FieldError {

    private String objectName;

    private String field;

    private String message;

    @Builder
    private FieldError(String objectName, String field, String message) {
        this.objectName = objectName;
        this.field = field;
        this.message = message;
    }
}
```

* **Gestion des exceptions : créer une exception personnalisée**

Spring prend en charge la gestion des exceptions par :
-	Un gestionnaire d'exceptions global (@ExceptionHandler )
-	Controller Advice (@ControllerAdvice )

L’annotation @ControllerAdvice est la spécialisation de l’annotation @Component afin qu'elle soit détectée automatiquement via l'analyse du chemin de classe. Un Conseil de Contrôleur est une sorte d'intercepteur qui entoure la logique de nos Contrôleurs et nous permet de leur appliquer une logique commune.

Les méthodes (annotées avec @ExceptionHandler) sont partagées globalement entre plusieurs composants @Controller pour capturer les exceptions et les traduire en réponses HTTP.

L’annotation @ExceptionHandler indique quel type d'exception nous voulons gérer. L'instance exception et le request seront injectés via des arguments de méthode.
 
 ⇨	En utilisant deux annotations ensemble, nous pouvons : contrôler le corps de la réponse avec le code d'état et gérer plusieurs exceptions dans la même méthode.

* Nous allons lancer une exception pour la ressource introuvable dans le contrôleur Spring Boot.Créons une classe ResourceNotFoundException qui étend RuntimeException.

```java
/**
 * ResourceNotFoundException class extends RuntimeException.
 * It's about a custom exception :
 * throwing an exception for resource not found in Spring Boot Service
 * ResourceNotFoundException is thrown with Http 404
 */

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

* La classe ServiceExceptionHandler gère deux exceptions spécifiques (ResoureNotFoundException et MethodArgumentNotValidException) et les exceptions globales à un seul endroit.
 
```java
@ControllerAdvice
public class ServiceExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SongServiceImpl.class);


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        ErrorMessage message = ErrorMessage.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timeStamp(new Date())
                .message(e.getMessage())
                .description(request.getDescription(false))
                .build();

        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public final ResponseEntity<ErrorMessage> handleArgumentNotValidException(MethodArgumentNotValidException e, WebRequest request) {

        BindingResult result = e.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors().stream()
                .map(f -> FieldError.builder()
                        .objectName(f.getObjectName())
                        .field(f.getField())
                        .message(f.getCode() + ": " + f.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ErrorMessage message = ErrorMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timeStamp(new Date())
                .message(e.getMessage())
                .description(request.getDescription(false))
                .fieldErrors(fieldErrors)
                .build();
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> globalException(Exception e, WebRequest request) {
        ErrorMessage message = ErrorMessage.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timeStamp(new Date())
                .message(e.getMessage())
                .description(request.getDescription(false))
                .build();

        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

## IX. Tests
Pour les tests unitaires et tests d'intégration, nous allons utiliser une base de données en mémoire H2 comme source de données pour les tests

Dons, nous allons créer un fichier **application.properties** pour les tests sous test/resources

![Screenshot](src/main/resources/images/resource-test.PNG)

```yaml
spring.datasource.url=jdbc:h2:mem:test_playlist_song_db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=playlistadmin
spring.datasource.password=admin
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
```

##### Tests Unitaires


* **Repository :**

**@DataJpaTest** : Pour tester les référentiels Spring Data JPA, ou tout autre composant lié à JPA, Spring Boot fournit l'annotation **@DataJpaTest**. Nous pouvons simplement l'ajouter à notre test unitaire et il configurera un contexte d'application Spring.

Il désactivera la configuration automatique complète, puis n'appliquera que la configuration d'activation pertinente pour les tests JPA. Par défaut, les tests annotés avec **@DataJpaTest** sont transactionnels et sont annulés à la fin de chaque test.

**@AutoConfigureTestDatabase** : nous devons dire au framework de test Spring qu'il ne devrait pas essayer de remplacer notre base de données. Nous pouvons le faire en utilisant l'annotation 

@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)

**@Before** Pour tester la logique de la base de données, nous avons initialement besoin de données avec lesquelles travailler, nous pouvons le faire en construisant manuellement les objets et en les enregistrant dans la base de données à l'aide de Java dans la section @Before 🡺 Ceci est utile lorsque nous voulons exécuter du code commun avant d'exécuter un test.

##### SongRepositoryTest

```java
@DataJpaTest
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SongRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(SongRepositoryTest.class);

    @Autowired
    SongRepository songRepository;
    private Song savedSong;

    @Before
    public void setupCreateSong() {
        Song song = new Song();
        song.setTitle("For The Lover That I Lost");
        song.setDescription("Live At Abbey Road Studios");
        song.setCategory(SongCategory.POP);
        song.setArtistName("Sam Smith");
        song.setDuration("3:01");

        savedSong = songRepository.save(song);
        assertThat(savedSong).isNotNull();
        assertThat(savedSong).hasFieldOrPropertyWithValue("title", "For The Lover That I Lost");
        assertThat(savedSong).hasFieldOrPropertyWithValue("description", "Live At Abbey Road Studios");
        assertThat(savedSong).hasFieldOrPropertyWithValue("category", SongCategory.POP);
        assertThat(savedSong).hasFieldOrPropertyWithValue("duration", "3:01");
        assertThat(savedSong).hasFieldOrPropertyWithValue("artistName", "Sam Smith");
    }

    @Test
    public void shouldFindAllSongs() {
        List<Song> songs = songRepository.findAll();
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        assertThat(songs).contains(songs.get(songs.size() - 1));
        assertThat(songs.get(songs.size() - 1).getId()).isNotNull();
    }

    @Test
    public void shouldFindSongsByCategory() {
        List<Song> songs = songRepository.findSongsByCategory(savedSong.getCategory());
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        assertThat(songs).contains(savedSong);
    }

    @Test
    public void shouldFindSongsByArtistName() {
        List<Song> songs = songRepository.findSongsByArtistNameContaining(savedSong.getArtistName());
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        assertThat(songs).contains(savedSong);
    }

    @Test
    public void shouldFindSongById() {
        Song foundSong = songRepository.findById(savedSong.getId()).orElse(null);
        assertThat(foundSong).isNotNull();
        assertThat(foundSong).isEqualTo(savedSong);
        assertThat(foundSong).hasFieldOrPropertyWithValue("title", savedSong.getTitle());
        assertThat(foundSong).hasFieldOrPropertyWithValue("description", savedSong.getDescription());
        assertThat(foundSong).hasFieldOrPropertyWithValue("category", savedSong.getCategory());
        assertThat(foundSong).hasFieldOrPropertyWithValue("duration", savedSong.getDuration());
        assertThat(foundSong).hasFieldOrPropertyWithValue("artistName", savedSong.getArtistName());
    }

    @Test
    public void shouldCreateSong() {

        int sizeBeforeCreate = songRepository.findAll().size();


        Song songToSave = new Song();
        songToSave.setTitle("The Falls");
        songToSave.setDescription("Album musical d'Ennio Morricone");
        songToSave.setCategory(SongCategory.CLASSICAL);
        songToSave.setArtistName("Morricone");
        songToSave.setDuration("7:10");
        songToSave.setPlaylist(savedSong.getPlaylist());

        Song song = songRepository.save(songToSave);

        int sizeAfterCreate = songRepository.findAll().size();
        assertThat(sizeAfterCreate).isEqualTo(sizeBeforeCreate + 1);
        assertThat(song).isNotNull();
        assertThat(song).hasFieldOrPropertyWithValue("title", "The Falls");
        assertThat(song).hasFieldOrPropertyWithValue("description", "Album musical d'Ennio Morricone");
        assertThat(song).hasFieldOrPropertyWithValue("category", SongCategory.CLASSICAL);
        assertThat(song).hasFieldOrPropertyWithValue("duration", "7:10");
        assertThat(song).hasFieldOrPropertyWithValue("artistName", "Morricone");

    }

    @Test
    public void shouldUpdateSong() {

        Song foundSong = songRepository.getById(savedSong.getId());
        assertThat(foundSong).isNotNull();

        foundSong.setTitle("Power");
        foundSong.setDescription("power album");
        foundSong.setArtistName("Isak Danielson");
        Song updatedSong = songRepository.save(foundSong);

        Song checkSong = songRepository.getById(updatedSong.getId());

        assertThat(checkSong.getId()).isNotNull();
        assertThat(checkSong.getId()).isEqualTo(updatedSong.getId());
        assertThat(checkSong.getTitle()).isEqualTo(updatedSong.getTitle());
        assertThat(checkSong.getDescription()).isEqualTo(updatedSong.getDescription());
        assertThat(checkSong.getCategory()).isEqualTo(updatedSong.getCategory());
        assertThat(checkSong.getDuration()).isEqualTo(updatedSong.getDuration());
        assertThat(checkSong.getArtistName()).isEqualTo(updatedSong.getArtistName());
    }

    @Test
    public void shouldDeleteSonById() {
        int sizeBeforeDelete = songRepository.findAll().size();

        Song foundSong = songRepository.getById(savedSong.getId());
        assertThat(foundSong).isNotNull();

        songRepository.deleteById(foundSong.getId());

        int sizeAfterDelete = songRepository.findAll().size();

        assertThat(sizeAfterDelete).isEqualTo(sizeBeforeDelete - 1);
    }
}
```

##### PlaylistRepositoryTest

```java
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
```
* **Service**

 **SongServiceUnitTest**
```java
@RunWith(SpringRunner.class)
public class SongServiceUnitTest {

    private final static Logger log = LoggerFactory.getLogger(SongServiceUnitTest.class);

    @MockBean
    private SongRepository songRepository;

    private SongServiceImpl songService;

    private Song mySong;
    private List<Song> songList = new ArrayList<>();


    @Before
    public void setup() {
        songService = new SongServiceImpl(songRepository);

        mySong = new Song();
        mySong.setId(1000L);
        mySong.setTitle("For The Lover That I Lost");
        mySong.setDescription("Live At Abbey Road Studios");
        mySong.setCategory(SongCategory.POP);
        mySong.setArtistName("Sam Smith");
        mySong.setDuration("3:01");
    }

    @Test
    public void testGetAllSongs() {
        songRepository.save(mySong);
        when(songRepository.findAll()).thenReturn(songList);

        //test
        List<Song> songs = songService.getAllSongs();
        assertEquals(songs, songList);
        verify(songRepository, times(1)).save(mySong);
        verify(songRepository, times(1)).findAll();
    }

    @Test
    public void testGetSongsByCategory() {
        songList.add(mySong);
        when(songRepository.findSongsByCategory(SongCategory.POP)).thenReturn(songList);

        //test
        List<Song> songs = songService.getSongsByCategory("POP");
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        verify(songRepository, times(1)).findSongsByCategory(SongCategory.POP);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistCategory() {
        List<Song> songs = songService.getSongsByCategory("Popy");
        assertTrue(songs.isEmpty());
    }

    @Test
    public void testGetSongsByArtistName() {
        songList.add(mySong);
        when(songRepository.findSongsByArtistNameContaining(mySong.getArtistName())).thenReturn(songList);
        List<Song> songs = songService.getSongsByArtistName(mySong.getArtistName());

        //test
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        verify(songRepository, times(1)).findSongsByArtistNameContaining(mySong.getArtistName());
    }

    @Test
    public void testCreateSong() {
        when(songRepository.save(any(Song.class))).thenReturn(mySong);
        songService.createSong(mySong);
        verify(songRepository, times(1)).save(any(Song.class));
    }

    @Test
    public void testUpdateSong() {
        when(songRepository.findById(mySong.getId())).thenReturn(Optional.of(mySong));

        mySong.setTitle("Power");
        mySong.setDescription("power album");
        mySong.setArtistName("Isak Danielson");

        given(songRepository.saveAndFlush(mySong)).willReturn(mySong);

        Song updatedSong = songService.updateSong(mySong);

        assertThat(updatedSong).isNotNull();
        assertThat(updatedSong).isEqualTo(mySong);
        assertThat(updatedSong.getId()).isNotNull();
        assertThat(updatedSong.getId()).isEqualTo(mySong.getId());
        assertThat(updatedSong.getTitle()).isEqualTo(mySong.getTitle());
        assertThat(updatedSong.getDescription()).isEqualTo(mySong.getDescription());
        assertThat(updatedSong.getCategory()).isEqualTo(mySong.getCategory());
        assertThat(updatedSong.getDuration()).isEqualTo(mySong.getDuration());
        assertThat(updatedSong.getArtistName()).isEqualTo(mySong.getArtistName());
    }


    @Test(expected = ResourceNotFoundException.class)
    public void testUpdateSongWithNonExistingId() {
        when(songRepository.findById(mySong.getId())).thenReturn(Optional.empty());
        songService.updateSong(mySong);

    }

    @Test
    public void testGetSongsById() {
        // when
        when(songRepository.findById(mySong.getId())).thenReturn(Optional.ofNullable(mySong));
        Song foundSong = songService.getSongById(mySong.getId());

        //test - then
        assertThat(foundSong).isNotNull();
        assertThat(foundSong).isEqualTo(mySong);
        assertThat(foundSong.getId()).isNotNull();
        assertThat(foundSong.getId()).isEqualTo(1000L);
        assertThat(foundSong.getId()).isEqualTo(mySong.getId());
        assertThat(foundSong.getTitle()).isEqualTo(mySong.getTitle());
        assertThat(foundSong.getDescription()).isEqualTo(mySong.getDescription());
        assertThat(foundSong.getCategory()).isEqualTo(mySong.getCategory());
        assertThat(foundSong.getDuration()).isEqualTo(mySong.getDuration());
        assertThat(foundSong.getArtistName()).isEqualTo(mySong.getArtistName());

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistingId() {

        // when
        when(songRepository.findById(4000L)).thenReturn(Optional.empty());
        songService.getSongById(4000L);
    }

    @Test
    public void testGetSongsWithNonExistingIdV2() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> songService.getSongById(4000L));

        assertThat(ex.getMessage()).isEqualTo("Not found song with id = 4000");
    }

    @Test
    public void testDeleteSongById() {
        when(songRepository.findById(mySong.getId())).thenReturn(Optional.of(mySong));
        songService.deleteSongById(mySong.getId());
        verify(songRepository, times(1)).deleteById(mySong.getId());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testDeleteSongWithNonExistingId() {
        when(songRepository.findById(4000L)).thenReturn(Optional.empty());
        songService.deleteSongById(4000L);
    }
}
```
 **PlaylistServiceUnitTest**
 
```java
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
```    
* **Resource**

 **SongResourceUnitTest**
    
```java
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SongResource.class)
public class SongResourceUnitTest {

    private static final Logger log = LoggerFactory.getLogger(SongResourceUnitTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ISongService songService;

    private Song mySong;
    private List<Song> songList = new ArrayList<>();


    @Before
    public void setup() {
        mySong = new Song();
        mySong.setTitle("For The Lover That I Lost");
        mySong.setDescription("Live At Abbey Road Studios");
        mySong.setCategory(SongCategory.POP);
        mySong.setArtistName("Sam Smith");
        mySong.setDuration("3:01");
    }

    @Test
    public void testGetAllSongs() throws Exception {
        songList.add(mySong);
        when(songService.getAllSongs()).thenReturn(songList);

        mockMvc.perform(get("/api/songs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].title").value(songList.get(0).getTitle()))
                .andExpect(jsonPath("$[*].description").value(songList.get(0).getDescription()))
                .andExpect(jsonPath("$[*].category").value(songList.get(0).getCategory().toString()))
                .andExpect(jsonPath("$[*].artistName").value(songList.get(0).getArtistName()))
                .andExpect(jsonPath("$[*].duration").value(songList.get(0).getDuration()));
        verify(songService).getAllSongs();
        verify(songService, times(1)).getAllSongs();
    }

    @Test
    public void testGetNoContentSongs() throws Exception {
        when(songService.getAllSongs()).thenReturn(songList);

        mockMvc.perform(get("/api/songs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetSongsByCategory() throws Exception {
        songList.add(mySong);
        when(songService.getSongsByCategory("POP")).thenReturn(songList);

        mockMvc.perform(get("/api/songs/category/" + mySong.getCategory())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].title").value(songList.get(0).getTitle()))
                .andExpect(jsonPath("$[*].description").value(songList.get(0).getDescription()))
                .andExpect(jsonPath("$[*].category").value(songList.get(0).getCategory().toString()))
                .andExpect(jsonPath("$[*].artistName").value(songList.get(0).getArtistName()))
                .andExpect(jsonPath("$[*].duration").value(songList.get(0).getDuration()));
    }

    @Test
    public void testGetNoContentSongsByCategory() throws Exception {
        when(songService.getSongsByCategory("CLASSICAL")).thenReturn(songList);

        mockMvc.perform(get("/api/songs/category/CLASSICAL")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }


    @Test
    public void testGetSongsWithNonExistingCategory() throws Exception {
        doThrow(new ResourceNotFoundException("Not found Category with value = popy")).when(songService).getSongsByCategory("popy");
        mockMvc.perform(get("/api/songs/category/popy")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found Category with value = popy"));
    }


    @Test
    public void testGetSongsByArtistName() throws Exception {
        songList.add(mySong);
        when(songService.getSongsByArtistName(mySong.getArtistName())).thenReturn(songList);

        mockMvc.perform(get("/api/songs/artist/" + mySong.getArtistName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].title").value(songList.get(0).getTitle()))
                .andExpect(jsonPath("$[*].description").value(songList.get(0).getDescription()))
                .andExpect(jsonPath("$[*].category").value(songList.get(0).getCategory().toString()))
                .andExpect(jsonPath("$[*].artistName").value(songList.get(0).getArtistName()))
                .andExpect(jsonPath("$[*].duration").value(songList.get(0).getDuration()));
    }

    @Test
    public void testGetNoContentSongsByArtistName() throws Exception {
        when(songService.getSongsByArtistName("Isak")).thenReturn(songList);

        mockMvc.perform(get("/api/songs/artist/Isak")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetSongById() throws Exception {
        mySong.setId(1000L);
        when(songService.getSongById(mySong.getId())).thenReturn(mySong);

        mockMvc.perform(get("/api/songs/" + mySong.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(mySong.getTitle()))
                .andExpect(jsonPath("$.description").value(mySong.getDescription()))
                .andExpect(jsonPath("$.category").value(mySong.getCategory().toString()))
                .andExpect(jsonPath("$.artistName").value(mySong.getArtistName()))
                .andExpect(jsonPath("$.duration").value(mySong.getDuration()));
    }


    @Test
    public void testGetSongByNonExistingId() throws Exception {
        doThrow(new ResourceNotFoundException("Not found Song with id = 1000")).when(songService).getSongById(1000L);
        mockMvc.perform(get("/api/songs/1000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found Song with id = 1000"));
    }


    @Test
    public void testCreateSong() throws Exception {
        when(songService.createSong(any(Song.class))).thenReturn(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().isCreated());
        verify(songService, times(1)).createSong(any());
    }

    @Test
    public void testCreateSongWithTitleSizeLessThanThree() throws Exception {
        mySong.setTitle("S");
        doThrow(new ResourceNotFoundException("Size: titre doit être compris entre 3 et 50 caractères"))
                .when(songService).createSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: titre doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testCreateSongWithDescriptionSizeLessThanThree() throws Exception {
        mySong.setDescription("S");
        doThrow(new ResourceNotFoundException("Size: description doit être compris entre 3 et 50 caractères"))
                .when(songService).createSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: description doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testCreateSongWithTitleNull() throws Exception {
        mySong.setTitle(null);
        doThrow(new ResourceNotFoundException("NotBlank: titre ne doit pas être null ou vide"))
                .when(songService).createSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("NotBlank: titre ne doit pas être null ou vide"));
    }


    @Test
    public void testUpdateSong() throws Exception {
        mySong.setId(1000L);
        when(songService.updateSong(mySong)).thenReturn(mySong);
        mockMvc.perform(put("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateSongWithTitleSizeLessThanThree() throws Exception {
        mySong.setId(1000L);
        mySong.setTitle("S");
        doThrow(new ResourceNotFoundException("Size: titre doit être compris entre 3 et 50 caractères"))
                .when(songService).updateSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: titre doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testUpdateSongWithDescriptionSizeLessThanThree() throws Exception {
        mySong.setId(1000L);
        mySong.setDescription("S");
        doThrow(new ResourceNotFoundException("Size: description doit être compris entre 3 et 50 caractères"))
                .when(songService).updateSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: description doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testUpdateSongWithTitleNull() throws Exception {
        mySong.setId(1000L);
        mySong.setTitle(null);
        doThrow(new ResourceNotFoundException("NotBlank: titre ne doit pas être null ou vide"))
                .when(songService).updateSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("NotBlank: titre ne doit pas être null ou vide"));
    }

    @Test
    public void testDeleteSongById() throws Exception {
        mySong.setId(1000L);
        doNothing().when(songService).deleteSongById(mySong.getId());
        mockMvc.perform(delete("/api/songs/" + mySong.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNotFoundSong() throws Exception {
        doThrow(new ResourceNotFoundException("Not found Song with id = 1000")).when(songService).deleteSongById(1000L);
        mockMvc.perform(delete("/api/songs/1000"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found Song with id = 1000"));
    }
}
```
 **PlaylistResourceUnitTest**
 
```java
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
    public void testGetNoContentPlaylists() throws Exception {
        when(playlistService.getAllPlaylists()).thenReturn(playlistList);

        mockMvc.perform(get("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
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
    public void testGetNoContentPlaylistsByTitle() throws Exception {
        when(playlistService.getPlaylistsByTitle("No Title")).thenReturn(playlistList);

        mockMvc.perform(get("/api/playlists/title/No Title")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
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
``` 
##### Tests d'intégration
    
**@SpringBootTest** :

-	Est une annotation fournie par Spring Boot

-	Elle permet lors de l’exécution des tests d’initialiser le contexte Spring.

-	Les beans de notre application peuvent alors être utilisés

-	Rappelons qu’un test s’exécute de façon unitaire, presque comme une application à part entière. Par défaut, notre test n’a donc aucune connaissance du contexte Spring. Dans le cas d’une application Spring Boot, c’est un vrai problème !

🡺 Le problème est résolu grâce à l’annotation @SpringBootTest.

* **Service**

   **SongServiceIntegrationTest**
    
```java
@SpringBootTest
@Transactional
@RunWith(SpringRunner.class)
public class SongServiceIntegrationTest {

    private final static Logger log = LoggerFactory.getLogger(SongServiceIntegrationTest.class);

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SongServiceImpl songService;

    private Song defaultSong;

    @Before
    public void setup() {
        Song mySong = new Song();
        mySong.setId(1000L);
        mySong.setTitle("For The Lover That I Lost");
        mySong.setDescription("Live At Abbey Road Studios");
        mySong.setCategory(SongCategory.POP);
        mySong.setArtistName("Sam Smith");
        mySong.setDuration("3:01");

        defaultSong = songRepository.saveAndFlush(mySong);

    }

    @Test
    public void testGetAllSongs() {
        List<Song> songs = songService.getAllSongs();
        assertThat(songs).isNotNull().isNotEmpty();
    }

    @Test
    public void testGetSongsByCategory() {
        List<Song> songs = songService.getSongsByCategory("POP");
        assertThat(songs).isNotNull().isNotEmpty();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistingCategory() {
        songService.getSongsByCategory("Popy");
    }

    @Test
    public void testGetSongsWithNonExistingCategoryV2() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> songService.getSongById(4000L));

        assertThat(ex.getMessage()).isEqualTo("Not found song with id = 4000");
    }

    @Test
    public void testGetSongsByArtistName() {
        List<Song> songs = songService.getSongsByArtistName("Sam Smith");
        assertThat(songs).isNotNull().isNotEmpty();
    }

   @Test
    public void testGetSongById() {
        Song song = songService.getSongById(defaultSong.getId());
        assertThat(song).isNotNull();
        assertThat(song.getId()).isNotNull();
        assertThat(song.getId()).isEqualTo(defaultSong.getId());
        assertThat(song.getTitle()).isEqualTo(defaultSong.getTitle());
        assertThat(song.getDescription()).isEqualTo(defaultSong.getDescription());
        assertThat(song.getCategory()).isEqualTo(defaultSong.getCategory());
        assertThat(song.getArtistName()).isEqualTo(defaultSong.getArtistName());
        assertThat(song.getDuration()).isEqualTo(defaultSong.getDuration());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongWithNonExistingId() {
        songService.getSongById(4000L);
    }

    @Test
    public void testCreateSong() {
        Song savedSong = songService.createSong(defaultSong);
        assertThat(savedSong).isNotNull();
        assertThat(savedSong.getId()).isNotNull();
        assertThat(savedSong.getTitle()).isEqualTo(defaultSong.getTitle());
        assertThat(savedSong.getDescription()).isEqualTo(defaultSong.getDescription());
        assertThat(savedSong.getCategory()).isEqualTo(defaultSong.getCategory());
        assertThat(savedSong.getDuration()).isEqualTo(defaultSong.getDuration());
        assertThat(savedSong.getArtistName()).isEqualTo(defaultSong.getArtistName());
    }

    @Test
    public void testUpdateSong() {
        defaultSong.setTitle("Broken");
        defaultSong.setDescription("Isak Album");
        defaultSong.setArtistName("Isak Danielson");

        Song updatedSong = songService.updateSong(defaultSong);
        assertThat(updatedSong).isNotNull();
        assertThat(updatedSong.getId()).isNotNull();
        assertThat(updatedSong.getTitle()).isEqualTo("Broken");
        assertThat(updatedSong.getDescription()).isEqualTo("Isak Album");
        assertThat(updatedSong.getCategory()).isEqualTo(defaultSong.getCategory());
        assertThat(updatedSong.getDuration()).isEqualTo(defaultSong.getDuration());
        assertThat(updatedSong.getArtistName()).isEqualTo("Isak Danielson");

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testUpdateSongWithNonExistingId() {
        defaultSong.setId(4000L);
        songService.updateSong(defaultSong);

    }

    @Test
    public void testDeleteSongById() {
        songService.deleteSongById(defaultSong.getId());
        Optional<Song> deletedSong = songRepository.findById(defaultSong.getId());
        assertThat(deletedSong.isPresent()).isFalse();

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testDeleteSongWithNonExistingId() {
        songService.deleteSongById(4000L);

    }
}
```
   **PlaylistServiceIntegrationTest**

```java
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
```
* **Resource**

Depuis la version 3.2 Spring introduit le framework de test MVC (MockMvc).
Nous avons mis en place le MockMvc. Le MockMvcBuilders.standaloneSetup() permet d'enregistrer un ou plusieurs contrôleurs sans avoir besoin d'utiliser le fichier WebApplicationContext.

La méthode perform permet d’envoyer la requête au serveur Rest. La méthode jsonPath permet d’accéder au contenu de la réponse json.

   **SongResourceIntegrationTest**

```java
@SpringBootTest
@RunWith(SpringRunner.class)
public class SongResourceIntegrationTest {
    private final static Logger log = LoggerFactory.getLogger(SongResourceIntegrationTest.class);

    private MockMvc mockMvc;

    @Autowired
    private ServiceExceptionHandler serviceExceptionHandler;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private ISongService songService;

    private Song mySong;

    @Before
    public void setup() {

        SongResource songResource = new SongResource(songService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(songResource)
                .setControllerAdvice(serviceExceptionHandler)
                .build();


        mySong =  new Song();
        mySong.setTitle("For The Lover That I Lost");
        mySong.setDescription("Live At Abbey Road Studios");
        mySong.setCategory(SongCategory.POP);
        mySong.setArtistName("Sam Smith");
        mySong.setDuration("3:01");
    }

    @Test
    public void testGetAllSongs() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        mockMvc.perform(get("/api/songs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].title").value(hasItem(savedSong.getTitle())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(savedSong.getDescription())))
                .andExpect(jsonPath("$.[*].category").value(hasItem(savedSong.getCategory().toString())))
                .andExpect(jsonPath("$.[*].duration").value(hasItem(savedSong.getDuration())))
                .andExpect(jsonPath("$.[*].artistName").value(hasItem(savedSong.getArtistName())));
    }

    @Test
    public void testGetSongsByCategory() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        mockMvc.perform(get("/api/songs/category/{category}", savedSong.getCategory().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].title").value(hasItem(savedSong.getTitle())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(savedSong.getDescription())))
                .andExpect(jsonPath("$.[*].category").value(hasItem(savedSong.getCategory().toString())))
                .andExpect(jsonPath("$.[*].duration").value(hasItem(savedSong.getDuration())))
                .andExpect(jsonPath("$.[*].artistName").value(hasItem(savedSong.getArtistName())));
    }

    @Test
    public void testGetSongsWithNonExistingCategory() throws Exception {
        mockMvc.perform(get("/api/songs/category/popy")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found Category with value = popy"));
    }


    @Test
    public void testGetSongsByArtistName() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);

        mockMvc.perform(get("/api/songs/artist/{artist}", savedSong.getArtistName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].title").value(hasItem(savedSong.getTitle())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(savedSong.getDescription())))
                .andExpect(jsonPath("$.[*].category").value(hasItem(savedSong.getCategory().toString())))
                .andExpect(jsonPath("$.[*].duration").value(hasItem(savedSong.getDuration())))
                .andExpect(jsonPath("$.[*].artistName").value(hasItem(savedSong.getArtistName())));
    }

    @Test
    public void testGetSongById() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        mockMvc.perform(get("/api/songs/{id}", savedSong.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedSong.getId()))
                .andExpect(jsonPath("$.title").value(savedSong.getTitle()))
                .andExpect(jsonPath("$.description").value(savedSong.getDescription()))
                .andExpect(jsonPath("$.category").value(savedSong.getCategory().toString()))
                .andExpect(jsonPath("$.duration").value(savedSong.getDuration()))
                .andExpect(jsonPath("$.artistName").value(savedSong.getArtistName()));
    }


    @Test
    public void testGetSongByNonExistingId() throws Exception {
        mockMvc.perform(get("/api/songs/4000"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testCreateSong() throws Exception {
        int sizeBefore = songRepository.findAll().size();
        Song savedSong = songRepository.saveAndFlush(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedSong)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(savedSong.getTitle()))
                .andExpect(jsonPath("$.description").value(savedSong.getDescription()))
                .andExpect(jsonPath("$.category").value(savedSong.getCategory().toString()))
                .andExpect(jsonPath("$.duration").value(savedSong.getDuration()))
                .andExpect(jsonPath("$.artistName").value(savedSong.getArtistName()));

        List<Song> songs = songRepository.findAll();

        assertThat(songs).hasSize(sizeBefore + 1);

        Song lastSong = songs.get(songs.size() - 1);

        assertThat(lastSong.getTitle()).isEqualTo(savedSong.getTitle());
        assertThat(lastSong.getDescription()).isEqualTo(savedSong.getDescription());
        assertThat(lastSong.getCategory()).isEqualTo(savedSong.getCategory());
        assertThat(lastSong.getDuration()).isEqualTo(savedSong.getDuration());
        assertThat(lastSong.getArtistName()).isEqualTo(savedSong.getArtistName());
    }


    @Test
    public void testCreateSongWithTitleSizeLessThanThree() throws Exception {
        mySong.setTitle("S");
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: titre doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testCreateSongWithDescriptionSizeLessThanThree() throws Exception {
        mySong.setDescription("S");
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: description doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testCreateSongWithTitleNull() throws Exception {
        mySong.setTitle(null);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("NotBlank: titre ne doit pas être null ou vide"));
    }


    @Test
    public void testUpdateSong() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        savedSong.setTitle("Song updated");
        mockMvc.perform(put("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedSong)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateSongWithTitleSizeLessThanThree() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        savedSong.setTitle("S");
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedSong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: titre doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testUpdateSongWithDescriptionSizeLessThanThree() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        savedSong.setDescription("S");
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedSong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: description doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testUpdateSongWithTitleNull() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        savedSong.setTitle(null);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedSong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("NotBlank: titre ne doit pas être null ou vide"));
    }


    @Test
    public void testDeleteSongById() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        mockMvc.perform(delete("/api/songs/{id}", savedSong.getId()))
                .andExpect(status().isNoContent());

    }

    @Test
    public void testDeleteNotFoundSong() throws Exception {
        mockMvc.perform(delete("/api/songs/1000"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found song with id = 1000"));
    }
}
```
   **PlaylistResourceIntegrationTest**
   
```java
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
```

La classe **TestUtils** contient une méthode qui sert à convertir un objet Json en une chaîne de caractère.

```java

public class TestUtils {
    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```
