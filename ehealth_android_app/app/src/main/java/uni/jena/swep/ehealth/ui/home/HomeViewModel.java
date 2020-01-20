package uni.jena.swep.ehealth.ui.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import uni.jena.swep.ehealth.data_visualisation.RoomData;
import uni.jena.swep.ehealth.data_visualisation.StepGoal;
import uni.jena.swep.ehealth.data_visualisation.TotalStepDaily;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<Integer> steps_taken;
    private MutableLiveData<Integer> step_goal;
    private MutableLiveData<Integer> step_progress;
    private MutableLiveData<RoomData> room_data;

    public HomeViewModel() {
        this.steps_taken = new MutableLiveData<Integer>();
        this.step_goal = new MutableLiveData<Integer>();
        this.step_progress = new MutableLiveData<Integer>();
        this.room_data = new MutableLiveData<RoomData>();

        this.steps_taken.setValue(0);
        this.step_goal.setValue(0);
        this.step_progress.setValue(0);
        this.room_data.setValue(new RoomData());
    }

    protected void updateProgress() {
        if (this.steps_taken.getValue() != null && this.step_goal.getValue() != null) {
            int progress = 0;

            if (this.step_goal.getValue() != 0) {
                progress = (int) (((float) this.steps_taken.getValue() / (float) this.step_goal.getValue()) *100.0);
            }

            if (progress > 100) {
                progress = 100;
            }
            this.step_progress.setValue(progress);
        }
    }

    public MutableLiveData<Integer> getSteps_taken() {
        return steps_taken;
    }

    public void setSteps_taken(MutableLiveData<Integer> steps_taken) {
        this.steps_taken = steps_taken;
    }

    public MutableLiveData<Integer> getStep_goal() {
        return step_goal;
    }

    public void setStep_goal(MutableLiveData<Integer> step_goal) {
        this.step_goal = step_goal;
    }

    public MutableLiveData<Integer> getStep_progress() {
        return step_progress;
    }

    public void setStep_progress(MutableLiveData<Integer> step_progress) {
        this.step_progress = step_progress;
    }

    public MutableLiveData<RoomData> getRoom_data() {
        return room_data;
    }

    public void setRoom_data(MutableLiveData<RoomData> room_data) {
        this.room_data = room_data;
    }
}