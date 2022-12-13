package android.criminal_intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.*

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(),
    CrimeListFragment.Callbacks{

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_crime)
            val currentFragment =
                supportFragmentManager.findFragmentById(R.id.fragment_container)


            if (currentFragment == null) { // создает и закрепляет транзакцию фрагмента
                val fragment = CrimeListFragment()
                supportFragmentManager
                    .beginTransaction() // создает и возвращает экземпляр FragmentTransaction(этот класс использует динамический интерфейс)
                    .add(R.id.fragment_container, fragment)
                    .commit()
            }
        }

    override fun onCrimeSelected(crimeId: UUID) {
        val fragment = CrimeFragment.newInstance(crimeId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
