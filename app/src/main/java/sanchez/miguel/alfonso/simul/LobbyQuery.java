package sanchez.miguel.alfonso.simul;

class LobbyQuery {
    public String participant_image,participant_name,participant_state;

    public LobbyQuery(String participant_image, String participant_name, String participant_state) {
        this.participant_image = participant_image;
        this.participant_name = participant_name;
        this.participant_state = participant_state;
    }

    public String getParticipant_image() {
        return participant_image;
    }

    public void setParticipant_image(String participant_image) {
        this.participant_image = participant_image;
    }

    public String getParticipant_name() {
        return participant_name;
    }

    public void setParticipant_name(String participant_name) {
        this.participant_name = participant_name;
    }

    public String getParticipant_state() {
        return participant_state;
    }

    public void setParticipant_state(String participant_state) {
        this.participant_state = participant_state;
    }

    public LobbyQuery() {
    }
}
