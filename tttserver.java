// Open TCP Socket and wait for connection
// Open UDP socket and wait for connection

// Client will connect with hello message "HELO <version number> <client ID>"
// ex: "HELO 1 zach"
    // Can probably take in command line arguments for the identifier/username

// Server responds with receipt acknowledging the client "SESS <version number> <session ID>"
    // We could implement some sort of counter so each time a session is created it will use that "count" value and add 1 for the next 1

// Client has two options for playing
    // Create a new game:
        // "CREA <client ID>" to create a new game
        // Server will respond to client with "JOND <client ID> <game ID>"
    // Find a game:
        // "LIST CURR" Server will send a list of all games currently open to join
        // "LIST ALL" Server will send a list of all games running on the server
            // Server responds with "GAMS <List of games>
            // We could store games in like a hashmap, where the key is if it is currently open or not and the value is the game ID
            // Client will then pick a game and send "JOIN <game ID>" and server will respond with "JOND <client ID> <game ID>"

// Server needs to decide who goes first in game (can use math.rand)
// 

