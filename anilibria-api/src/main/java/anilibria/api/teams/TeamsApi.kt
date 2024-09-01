package anilibria.api.teams

import anilibria.api.teams.models.TeamsTeamResponse
import anilibria.api.teams.models.TeamsRoleResponse
import anilibria.api.teams.models.TeamsUserResponse
import retrofit2.http.GET

interface TeamsApi {

    @GET("/teams/")
    suspend fun getTeams(): List<TeamsTeamResponse>

    @GET("/teams/roles")
    suspend fun getRoles(): List<TeamsRoleResponse>

    @GET("/teams/users")
    suspend fun getUsers(): List<TeamsUserResponse>
}