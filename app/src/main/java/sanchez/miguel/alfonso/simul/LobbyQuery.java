package sanchez.miguel.alfonso.simul;

class LobbyQuery {
    public String participant_image;
    public String participant_name;
    public String participant_state;
    public String participant_speed;

    public String getParticipant_speed() {
        return participant_speed;
    }

    public void setParticipant_speed(String participant_speed) {
        this.participant_speed = participant_speed;
    }


    public LobbyQuery(String participant_image, String participant_name, String participant_state, String participant_speed) {
        this.participant_image = participant_image;
        this.participant_name = participant_name;
        this.participant_state = participant_state;
        this.participant_speed = participant_speed;
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
