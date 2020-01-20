package uni.jena.swep.ehealth.ui.movementdata;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MovementDataViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public MovementDataViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is movementdata fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
