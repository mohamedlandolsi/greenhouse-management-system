#!/bin/bash

# Script to generate sample data for measurements and actions
API_BASE="http://localhost:8080"

echo "Generating sample measurements data..."

# Generate measurements for the last 7 days
for day in {0..6}; do
    for hour in {8..18..2}; do
        # Temperature measurements (parametre_id: 1)
        temp_value=$(awk -v min=18 -v max=32 'BEGIN{srand(); print min+rand()*(max-min)}')
        temp_alerte=$( (( $(echo "$temp_value < 18 || $temp_value > 28" | bc -l) )) && echo "true" || echo "false")
        date_mesure=$(date -u -d "$day days ago $hour:$(shuf -i 0-59 -n 1):00" '+%Y-%m-%dT%H:%M:%S')
        
        curl -s -X POST "$API_BASE/api/environnement/mesures" \
            -H "Content-Type: application/json" \
            -d "{
                \"parametreId\": 1,
                \"valeur\": $temp_value,
                \"dateMesure\": \"$date_mesure\",
                \"alerte\": $temp_alerte
            }" > /dev/null
        
        # Humidity measurements (parametre_id: 2)
        humid_value=$(awk -v min=30 -v max=90 'BEGIN{srand(); print min+rand()*(max-min)}')
        humid_alerte=$( (( $(echo "$humid_value < 40 || $humid_value > 80" | bc -l) )) && echo "true" || echo "false")
        
        curl -s -X POST "$API_BASE/api/environnement/mesures" \
            -H "Content-Type: application/json" \
            -d "{
                \"parametreId\": 2,
                \"valeur\": $humid_value,
                \"dateMesure\": \"$date_mesure\",
                \"alerte\": $humid_alerte
            }" > /dev/null
        
        # Luminosity measurements (parametre_id: 3)
        lum_value=$(awk -v min=100 -v max=1000 'BEGIN{srand(); print min+rand()*(max-min)}')
        lum_alerte=$( (( $(echo "$lum_value < 200 || $lum_value > 800" | bc -l) )) && echo "true" || echo "false")
        
        curl -s -X POST "$API_BASE/api/environnement/mesures" \
            -H "Content-Type: application/json" \
            -d "{
                \"parametreId\": 3,
                \"valeur\": $lum_value,
                \"dateMesure\": \"$date_mesure\",
                \"alerte\": $lum_alerte
            }" > /dev/null
        
        echo "✓ Generated measurements for day -$day, hour $hour:00"
    done
done

echo -e "\nGenerating sample actions data..."

# Generate various actions over the last 7 days
actions=(
    '{"equipementId":1,"parametreId":1,"typeAction":"ACTIVER","valeurCible":null,"valeurActuelle":null,"statut":"EXECUTEE","dateExecution":"DATE_PLACEHOLDER","resultat":"Ventilateur activé avec succès"}'
    '{"equipementId":2,"parametreId":1,"typeAction":"AJUSTER","valeurCible":22.0,"valeurActuelle":25.5,"statut":"EXECUTEE","dateExecution":"DATE_PLACEHOLDER","resultat":"Température ajustée à 22°C"}'
    '{"equipementId":3,"parametreId":2,"typeAction":"ACTIVER","valeurCible":null,"valeurActuelle":null,"statut":"EXECUTEE","dateExecution":"DATE_PLACEHOLDER","resultat":"Pompe irrigation activée"}'
    '{"equipementId":4,"parametreId":3,"typeAction":"DESACTIVER","valeurCible":null,"valeurActuelle":null,"statut":"EXECUTEE","dateExecution":"DATE_PLACEHOLDER","resultat":"Éclairage désactivé (nuit)"}'
    '{"equipementId":2,"parametreId":1,"typeAction":"DESACTIVER","valeurCible":null,"valeurActuelle":null,"statut":"EXECUTEE","dateExecution":"DATE_PLACEHOLDER","resultat":"Chauffage désactivé"}'
    '{"equipementId":3,"parametreId":2,"typeAction":"AJUSTER","valeurCible":60.0,"valeurActuelle":45.3,"statut":"EXECUTEE","dateExecution":"DATE_PLACEHOLDER","resultat":"Humidité ajustée à 60%"}'
    '{"equipementId":4,"parametreId":3,"typeAction":"ACTIVER","valeurCible":null,"valeurActuelle":null,"statut":"EXECUTEE","dateExecution":"DATE_PLACEHOLDER","resultat":"Éclairage activé (jour)"}'
    '{"equipementId":1,"parametreId":1,"typeAction":"DESACTIVER","valeurCible":null,"valeurActuelle":null,"statut":"EXECUTEE","dateExecution":"DATE_PLACEHOLDER","resultat":"Ventilateur désactivé"}'
    '{"equipementId":2,"parametreId":1,"typeAction":"AJUSTER","valeurCible":20.0,"valeurActuelle":18.2,"statut":"EXECUTEE","dateExecution":"DATE_PLACEHOLDER","resultat":"Température ajustée à 20°C"}'
    '{"equipementId":3,"parametreId":2,"typeAction":"DESACTIVER","valeurCible":null,"valeurActuelle":null,"statut":"EXECUTEE","dateExecution":"DATE_PLACEHOLDER","resultat":"Pompe arrêtée"}'
    '{"equipementId":1,"parametreId":1,"typeAction":"ACTIVER","valeurCible":null,"valeurActuelle":null,"statut":"EN_ATTENTE","dateExecution":null,"resultat":null}'
    '{"equipementId":3,"parametreId":2,"typeAction":"AJUSTER","valeurCible":55.0,"valeurActuelle":52.0,"statut":"ECHOUEE","dateExecution":"DATE_PLACEHOLDER","resultat":"Erreur: Pompe non réactive"}'
)

counter=0
for day in {0..6}; do
    for hour in {9..17..4}; do
        if [ $counter -lt ${#actions[@]} ]; then
            date_execution=$(date -u -d "$day days ago $hour:$(shuf -i 0-59 -n 1):00" '+%Y-%m-%dT%H:%M:%S')
            action_json="${actions[$counter]//DATE_PLACEHOLDER/$date_execution}"
            
            curl -s -X POST "$API_BASE/api/controle/actions" \
                -H "Content-Type: application/json" \
                -d "$action_json" > /dev/null
            
            echo "✓ Created action #$((counter+1))"
            ((counter++))
        fi
    done
done

echo -e "\n✅ Sample data generation completed!"
echo "📊 Created approximately:"
echo "   - $(($counter)) actions"
echo "   - $((7 * 6 * 3)) measurements (temperature, humidity, luminosity)"
