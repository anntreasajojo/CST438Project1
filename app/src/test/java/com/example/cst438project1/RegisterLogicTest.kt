package com.example.cst438project1

import com.example.cst438project1.database.Activity
import com.example.cst438project1.database.Goal
import com.example.cst438project1.database.Sex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ValidationTest {
    // Nothing is wrong with a field the user has not filled in yet, so an empty
    // value never shows a message.
    @Test
    fun emptyFieldsAreNotYetErrors() {
        assertNull(usernameError(""))
        assertNull(passwordError(""))
        assertNull(confirmError("hunter2000", ""))
        assertNull(rangeError("", AGE_RANGE, "years"))
    }

    @Test
    fun usernameRules() {
        assertNull(usernameError("doyoung"))
        assertNull(usernameError("a_1"))
        assertNotNull(usernameError("ab"))
        assertNotNull(usernameError("a".repeat(21)))
        assertNotNull(usernameError("has space"))
        assertNotNull(usernameError("hyphen-ated"))
    }

    @Test
    fun passwordRules() {
        assertNull(passwordError("12345678"))
        assertNotNull(passwordError("1234567"))
    }

    @Test
    fun confirmMustMatch() {
        assertNull(confirmError("hunter2000", "hunter2000"))
        assertNotNull(confirmError("hunter2000", "hunter2001"))
        // A trailing space is a different password, and saying so beats a
        // failed login later.
        assertNotNull(confirmError("hunter2000", "hunter2000 "))
    }

    @Test
    fun rangesRejectWhatIsOutsideThem() {
        assertNull(rangeError("30", AGE_RANGE, "years"))
        assertNotNull(rangeError("12", AGE_RANGE, "years"))
        assertNotNull(rangeError("121", AGE_RANGE, "years"))
        assertNull(rangeError("180", HEIGHT_RANGE, "cm"))
        assertNotNull(rangeError("18", HEIGHT_RANGE, "cm"))
        assertNull(rangeError("75", WEIGHT_RANGE, "kg"))
        assertNotNull(rangeError("500", WEIGHT_RANGE, "kg"))
    }

    // What the Next button is wired to.
    @Test
    fun accountIsCompleteOnlyWhenEveryFieldPasses() {
        assertTrue(accountIsComplete("doyoung", "hunter2000", "hunter2000"))
        assertFalse(accountIsComplete("", "hunter2000", "hunter2000"))
        assertFalse(accountIsComplete("doyoung", "short", "short"))
        assertFalse(accountIsComplete("doyoung", "hunter2000", "hunter2001"))
        assertFalse(accountIsComplete("doyoung", "hunter2000", ""))
    }
}

class PasswordHasherTest {
    private val password = "correct horse battery"

    @Test
    fun sameSaltGivesSameHash() {
        val salt = PasswordHasher.newSalt()
        assertEquals(PasswordHasher.hash(password, salt), PasswordHasher.hash(password, salt))
    }

    // Without this, two people who picked the same password would be visibly
    // identical in the database.
    @Test
    fun differentSaltGivesDifferentHash() {
        assertNotEquals(
            PasswordHasher.hash(password, PasswordHasher.newSalt()),
            PasswordHasher.hash(password, PasswordHasher.newSalt())
        )
    }

    @Test
    fun saltsAreNotReused() {
        val salts = List(50) { PasswordHasher.newSalt() }
        assertEquals(salts.size, salts.toSet().size)
    }

    @Test
    fun verifyAcceptsTheRightPasswordAndRejectsOthers() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(password, salt)

        assertTrue(PasswordHasher.verify(password, salt, hash))
        assertFalse(PasswordHasher.verify("wrong password", salt, hash))
        assertFalse(PasswordHasher.verify(password.uppercase(), salt, hash))
        assertFalse(PasswordHasher.verify("", salt, hash))
    }

    // The wrong salt is as good as the wrong password.
    @Test
    fun verifyFailsAgainstAnotherSalt() {
        val hash = PasswordHasher.hash(password, PasswordHasher.newSalt())
        assertFalse(PasswordHasher.verify(password, PasswordHasher.newSalt(), hash))
    }

    @Test
    fun hashIsNeverThePasswordItself() {
        val salt = PasswordHasher.newSalt()
        assertNotEquals(password, PasswordHasher.hash(password, salt))
    }
}

class NutritionTargetsTest {
    // Mifflin-St Jeor by hand: 10*75 + 6.25*180 - 5*30 + 5 = 1730 kcal at rest,
    // times 1.55 for moderate activity = 2681.5, maintained = 2682.
    @Test
    fun knownProfileMatchesHandCalculation() {
        assertEquals(1730.0, bmr(Sex.MALE, 75, 180, 30), 0.001)
        assertEquals(
            2682,
            targets(Sex.MALE, 75, 180, 30, Activity.MODERATE, Goal.MAINTAIN).calorieGoal
        )
    }

    // The macros have to add back up to the calorie target, or the two numbers
    // on the profile screen contradict each other.
    @Test
    fun macrosAddBackUpToTheCalorieGoal() {
        val profile = targets(Sex.FEMALE, 62, 165, 27, Activity.LIGHT, Goal.LOSE)
        val fromMacros = profile.carbGoal * 4 + profile.proteinGoal * 4 + profile.fatGoal * 9
        assertTrue(
            "macros totalled $fromMacros against a goal of ${profile.calorieGoal}",
            abs(fromMacros - profile.calorieGoal) <= profile.calorieGoal * 0.02
        )
    }

    @Test
    fun sexChangesTheResult() {
        val male = targets(Sex.MALE, 70, 175, 25, Activity.MODERATE, Goal.MAINTAIN)
        val female = targets(Sex.FEMALE, 70, 175, 25, Activity.MODERATE, Goal.MAINTAIN)
        val other = targets(Sex.OTHER, 70, 175, 25, Activity.MODERATE, Goal.MAINTAIN)

        assertNotEquals(male.calorieGoal, female.calorieGoal)
        assertTrue(other.calorieGoal in (female.calorieGoal + 1)..<male.calorieGoal)
    }

    @Test
    fun movingMoreRaisesTheTarget() {
        val calories = Activity.entries.map {
            targets(Sex.MALE, 80, 182, 22, it, Goal.MAINTAIN).calorieGoal
        }
        assertEquals(calories.sorted(), calories)
    }

    @Test
    fun losingIsBelowMaintainingIsBelowGaining() {
        fun goal(goal: Goal) = targets(Sex.FEMALE, 68, 170, 35, Activity.ACTIVE, goal).calorieGoal
        assertEquals(500, goal(Goal.MAINTAIN) - goal(Goal.LOSE))
        assertEquals(500, goal(Goal.GAIN) - goal(Goal.MAINTAIN))
    }

    // A deficit on someone small must not produce a target no one should eat.
    @Test
    fun theTargetHasAFloor() {
        assertEquals(1200, targets(Sex.FEMALE, 40, 145, 70, Activity.SEDENTARY, Goal.LOSE).calorieGoal)
    }
}
